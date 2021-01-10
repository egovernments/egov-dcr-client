package org.egov.client.edcr.feature;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.client.edcr.constants.DxfFileConstants_Pondicherry;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Building;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Plot;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.common.entity.edcr.SetBack;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RearYardService_Pondicherry extends GeneralRule {
	private static final Logger LOG = Logger.getLogger(RearYardService_Pondicherry.class);
	private static final String MINIMUMLABEL = "Minimum distance ";
	private static final BigDecimal REARYARDMINIMUM_DISTANCE_1 = BigDecimal.valueOf(1L);
	private static final BigDecimal REARYARDMINIMUM_DISTANCE_1_5 = BigDecimal.valueOf(1.5D);
	private static final BigDecimal REARYARDMINIMUM_DISTANCE_3 = BigDecimal.valueOf(3L);

	private static final String RULE_PART_TWO_TABLE_ONE = "Part-II Table-1";
	public static final String BSMT_REAR_YARD_DESC = "Basement Rear Setback";
	public static final BigDecimal ROAD_WIDTH_TWELVE_POINTTWO = BigDecimal.valueOf(12.2D);

	private class RearYardResult {
		String rule;
		String subRule;
		String blockName;
		Integer level;
		String occupancy;
		BigDecimal actualMinDistance = BigDecimal.ZERO;
		BigDecimal actualMeanDistance = BigDecimal.ZERO;
		BigDecimal expectedminimumDistance = BigDecimal.ZERO;
		BigDecimal expectedmeanDistance = BigDecimal.ZERO;
		boolean status = false;

		private RearYardResult() {
		}
	}

	public void processRearYard(Plan pl) {
		HashMap<String, String> errors = new HashMap<String, String>();
		Plot plot = pl.getPlot();
		if (plot == null) {
			return;
		}
		validateRearYard(pl);

		if (plot != null && !pl.getBlocks().isEmpty()) {
			for (Block block : pl.getBlocks()) {
				this.scrutinyDetail = new ScrutinyDetail();
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(1), "Byelaw");
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(2), "Level");
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(3), "Occupancy");
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(4), "Side Number");
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(5), "Field Verified");
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(6), "Permissible");
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(7), "Provided");
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(8), "Status");
				this.scrutinyDetail.setHeading("Rear Setback");
				RearYardResult rearYardResult = new RearYardResult();

				for (SetBack setback : block.getSetBacks()) {
					if (setback.getRearYard() != null) {
						BigDecimal min = setback.getRearYard().getMinimumDistance();
						// BigDecimal mean = setback.getRearYard().getMean();

						BigDecimal buildingHeight = (setback.getRearYard().getHeight() != null
								&& setback.getRearYard().getHeight().compareTo(BigDecimal.ZERO) > 0)
										? setback.getRearYard().getHeight() : block.getBuilding().getBuildingHeight();

						if (buildingHeight != null && min.doubleValue() > 0.0D) {
							// if (buildingHeight != null && (min.doubleValue() > 0.0D || mean.doubleValue()
							// > 0.0D)) {
							for (Occupancy occupancy : block.getBuilding().getTotalArea()) {
								this.scrutinyDetail.setKey("Block_" + block.getName() + "_Rear Setback");

								if (setback.getLevel().intValue() < 0) {
									this.scrutinyDetail.setKey("Block_" + block.getName() + "_Basement Rear Setback");
									// checkRearYardBasement(pl, block.getBuilding(), block.getName(),
									// setback.getLevel(), plot, "Basement Rear Setback", min, mean,
									// occupancy.getTypeHelper(), rearYardResult);
									checkRearYardBasement(pl, block.getBuilding(), block.getName(), setback.getLevel(),
											plot, "Basement Rear Setback", min, occupancy.getTypeHelper(), rearYardResult);
								}

								if ((occupancy.getTypeHelper().getSubtype() != null && ("A-R"
										.equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode())
										|| "A-AF".equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode())
										|| "A-PO".equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode())))
										|| "F".equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) {
									if (buildingHeight.compareTo(BigDecimal.valueOf(10L)) <= 0 
											&& block.getBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(3L)) <= 0) {
										checkRearYard(pl, block.getBuilding(), block, setback.getLevel(), plot,
												"Rear Setback", min, occupancy.getTypeHelper(), rearYardResult, buildingHeight);
										continue;
									}
								}
							}
						}
					}
					Map<String, String> details = new HashMap<String, String>();
					details.put("Byelaw", rearYardResult.subRule);
					details.put("Level", (rearYardResult.level != null) ? rearYardResult.level.toString() : "");
					details.put("Occupancy", rearYardResult.occupancy);
					if (rearYardResult.expectedmeanDistance != null
							&& rearYardResult.expectedmeanDistance.compareTo(BigDecimal.valueOf(0L)) == 0) {
						details.put("Field Verified", "Minimum distance ");
						details.put("Permissible", rearYardResult.expectedminimumDistance.toString());
						details.put("Provided", rearYardResult.actualMinDistance.toString());
					} else {
						details.put("Field Verified", "Minimum distance ");
						details.put("Permissible", rearYardResult.expectedminimumDistance.toString());
						details.put("Provided", rearYardResult.actualMinDistance.toString());
					}

					if (rearYardResult.status) {
						details.put("Status", Result.Accepted.getResultVal());
					} else {
						details.put("Status", Result.Not_Accepted.getResultVal());
					}
					this.scrutinyDetail.getDetail().add(details);
					pl.getReportOutput().getScrutinyDetails().add(this.scrutinyDetail);
				}
			}
		}
	}

	private void checkRearYard(Plan pl, Building building, Block block, Integer level, Plot plot,
			String rearYardFieldName, BigDecimal min, OccupancyTypeHelper mostRestrictiveOccupancy,
			RearYardResult rearYardResult, BigDecimal buildingHeight) {
		String rule = "Rear Setback";
		String subRule = "Part-II Table-1";
		Boolean valid = Boolean.valueOf(false);
		BigDecimal minVal = BigDecimal.valueOf(0L);
		BigDecimal meanVal = BigDecimal.valueOf(0L);
		String typeOfArea = pl.getPlanInformation().getTypeOfArea();

		if (mostRestrictiveOccupancy.getSubtype() != null
				&& ("A-R".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| "A-AF".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| "A-PO".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))) {
			if (pl.getPlanInformation() != null && StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& "RESIDENTIAL".equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())) {
				checkRearYardForResidential(pl, block, level, min, mostRestrictiveOccupancy, rearYardResult, subRule,
						rule, minVal, meanVal, typeOfArea, valid);
			}
		}
	}

	private void checkRearYardForResidential(Plan pl, Block block, Integer level, BigDecimal min,
			OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult, String subRule, String rule,
			BigDecimal minVal, BigDecimal meanVal, String typeOfArea, Boolean valid) {
		String plotType = "Regular";
		// getting additoinal property crz_area
		String crz = pl.getPlanInfoProperties().get("CRZ_AREA");
		// check it is ews
		Boolean ewsPlot = isEwsPlot(pl);
		Boolean CRZZone = false;
		if (ewsPlot) {
			plotType = "EWS";
		}
		String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);

		LOG.info("CRZ=" + pl.getPlanInformation().getCrzZoneArea());
		if (crzValue != null && crzValue.equalsIgnoreCase(DcrConstants.YES)) {
			CRZZone = true;
		}

		if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
			if (CRZZone) {
				switch (crz) {
				case DxfFileConstants_Pondicherry.CRZ2:
					if (ewsPlot) {
						minVal = REARYARDMINIMUM_DISTANCE_1;
						subRule = RULE_PART_TWO_TABLE_ONE;
					} else {
						minVal = REARYARDMINIMUM_DISTANCE_1_5;
						subRule = RULE_PART_TWO_TABLE_ONE;
					}
					break;
				case DxfFileConstants_Pondicherry.CRZ3:
					if (ewsPlot) {
						minVal = REARYARDMINIMUM_DISTANCE_1;
						subRule = RULE_PART_TWO_TABLE_ONE;
					} else {
						minVal = REARYARDMINIMUM_DISTANCE_1_5;
						subRule = RULE_PART_TWO_TABLE_ONE;
					}
					break;
				default:
					pl.addError("Not Implemented", "No Data for CRZ");
					break;
				}
			} else {
				if (ewsPlot) {
					minVal = REARYARDMINIMUM_DISTANCE_1;
					subRule = RULE_PART_TWO_TABLE_ONE;
				} else {
					minVal = REARYARDMINIMUM_DISTANCE_1_5;
					subRule = RULE_PART_TWO_TABLE_ONE;
				}
			}
		} else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)
				|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
				|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)) {
			minVal = BigDecimal.ZERO;
			subRule = DxfFileConstants_Pondicherry.NOT_APPLICABLE;
		} else {
			pl.addError("Not Implemented", "Not a valid classification area");
		}

		valid = validateMinimumAndMeanValue(min, minVal);

		compareRearYardResult(block.getName(), min, mostRestrictiveOccupancy, rearYardResult, valid, subRule, rule,
				minVal, level);
	}

	public Boolean isEwsPlot(Plan pl) {
		if (pl.getPlanInformation().getPlotArea().compareTo(BigDecimal.valueOf(100l)) < 0)
			return true;
		else
			return false;
	}

	private Boolean checkRearYardBasement(Plan plan, Building building, String blockName, Integer level, Plot plot,
			String rearYardFieldName, BigDecimal min, OccupancyTypeHelper mostRestrictiveOccupancy, RearYardResult rearYardResult) {
		Boolean valid = Boolean.valueOf(false);
		String subRule = "47";
		String rule = "Rear Setback";
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		if ((mostRestrictiveOccupancy.getSubtype() != null
				&& "A-R".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))
				|| "A-AF".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| "A-PO".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| "F".equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			if (plot.getArea().compareTo(BigDecimal.valueOf(300L)) <= 0) {
				minVal = REARYARDMINIMUM_DISTANCE_3;
				valid = validateMinimumAndMeanValue(min, minVal);
			}

			rule = "Basement Rear Setback";
			compareRearYardResult(blockName, min, mostRestrictiveOccupancy, rearYardResult, valid, subRule, rule,
					minVal, level);
		}
		return valid;
	}

	private Boolean validateMinimumAndMeanValue(BigDecimal min, BigDecimal minval) {
		Boolean valid = Boolean.valueOf(false);
		if (min.compareTo(minval) >= 0)
			valid = Boolean.valueOf(true);
		return valid;
	}

	private void validateRearYard(Plan pl) {
		for (Block block : pl.getBlocks()) {
			if (!block.getCompletelyExisting().booleanValue()) {
				Boolean rearYardDefined = Boolean.valueOf(false);
				for (SetBack setback : block.getSetBacks()) {
					if (setback.getRearYard() != null
							&& setback.getRearYard().getMean().compareTo(BigDecimal.valueOf(0L)) > 0) {
						rearYardDefined = Boolean.valueOf(true);
					}
				}
				if (!rearYardDefined.booleanValue()
						&& !pl.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase("YES")) {
					HashMap<String, String> errors = new HashMap<String, String>();
					errors.put("Rear Setback", prepareMessage("msg.error.not.defined",
							new String[] { "Rear Setback for Block " + block.getName() }));
					pl.addErrors(errors);
				}
			}
		}
	}

	private void compareRearYardResult(String blockName, BigDecimal min, OccupancyTypeHelper mostRestrictiveOccupancy,
			RearYardResult rearYardResult, Boolean valid, String subRule, String rule, BigDecimal minVal, Integer level) {
		String occupancyName;
		if (mostRestrictiveOccupancy.getSubtype() != null) {
			occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
		} else {
			occupancyName = mostRestrictiveOccupancy.getType().getName();
		}
		if (minVal.compareTo(rearYardResult.expectedminimumDistance) >= 0) {
			if (minVal.compareTo(rearYardResult.expectedminimumDistance) == 0) {
				rearYardResult.rule = (rearYardResult.rule != null) ? (rearYardResult.rule + "," + rule) : rule;
				rearYardResult.occupancy = (rearYardResult.occupancy != null)
						? (rearYardResult.occupancy + "," + occupancyName) : occupancyName;
			} else {
				rearYardResult.rule = rule;
				rearYardResult.occupancy = occupancyName;
			}

			rearYardResult.subRule = subRule;
			rearYardResult.blockName = blockName;
			rearYardResult.level = level;
			rearYardResult.expectedminimumDistance = minVal;
			rearYardResult.actualMinDistance = min;
			rearYardResult.status = valid.booleanValue();
		}
	}
}
