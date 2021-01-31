package org.egov.client.edcr.feature;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.client.edcr.constants.DxfFileConstants_Pondicherry;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.FeatureProcess;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AdditionalFeature_Pondicherry extends FeatureProcess
{
	private static final Logger LOG = Logger.getLogger(AdditionalFeature_Pondicherry.class);
	private static final String RULE = "Part-II Table-1 Amended 09/04/2020";
	private static final BigDecimal ONE = BigDecimal.valueOf(1L);
	private static final BigDecimal TWO = BigDecimal.valueOf(2L);
	private static final BigDecimal THREE = BigDecimal.valueOf(3L);
  
	public static final String NO_OF_FLOORS = "Maximum number of floors allowed";
	public static final String HEIGHT_BUILDING = "Maximum height of building allowed";
	public static final String MIN_PLINTH_HEIGHT = " >= 0.45";
	public static final String MIN_PLINTH_HEIGHT_DESC = "Minimum plinth height";
	public static final String MAX_BSMNT_CELLAR = "Number of basement/cellar allowed";
	public static final String MIN_INT_COURT_YARD = "0.15";
	public static final String MIN_INT_COURT_YARD_DESC = "Minimum interior courtyard";
  
	public Plan validate(Plan pl) {
		HashMap<String, String> errors = new HashMap<String, String>();
		List<Block> blocks = pl.getBlocks();
		for (Block block : blocks) {
			if (block.getBuilding() != null && 
					block.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) == 0) {
				/*errors.put(String.format("Block %s building height", new Object[] { block.getNumber() }), this.edcrMessageSource
					.getMessage("msg.error.not.defined", new String[] {                
					String.format("Block %s building height", new Object[] { block.getNumber() })
					} LocaleContextHolder.getLocale()));*/
				pl.addErrors(errors);
			} 
		}
		return pl;
	}

	public Plan process(Plan pl) {
		HashMap<String, String> errors = new HashMap<String, String>();
		validate(pl);
		String typeOfArea = pl.getPlanInformation().getTypeOfArea();
		if (StringUtils.isNotBlank(typeOfArea)) {
			validateNumberOfFloors(pl, errors, typeOfArea);
		}    
		return pl;
	}
  
	private void validateNumberOfFloors(Plan pl, HashMap<String, String> errors, String typeOfArea) {
		for (Block block : pl.getBlocks()) {
			boolean isAccepted = false;
			ScrutinyDetail scrutinyDetail = getNewScrutinyDetail("Block_" + block.getNumber() + "_Number of Floors");
			BigDecimal floorAbvGround = block.getBuilding().getFloorsAboveGround();
			String requiredFloorCount = "";
			String actualFloorCount = "";
      
			String crz = pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.CRZ_AREA);
			String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
			Boolean ewsBuilding = isEwsBuilding(pl);
			Boolean ewsPlot = isEwsPlot(pl);
			Boolean stiltFloor = isStiltFloor(pl);
			Boolean basementFloor = isBasementFloor(block);
			Boolean CRZZone = false;

			LOG.info("CRZ=" + pl.getPlanInformation().getCrzZoneArea());
			if (crzValue != null && crzValue.equalsIgnoreCase(DcrConstants.YES)) {
				CRZZone = true;
			}
		
			if (CRZZone) {
				switch (crz) {
					case DxfFileConstants_Pondicherry.CRZ2:
						if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)) {
							if (ewsPlot) {
								if(ewsBuilding) {
									if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
										isAccepted = true;
										requiredFloorCount = "Maximum 2 Floors";
										actualFloorCount = floorAbvGround.toString() + " Floors";
									}
									else
									{
										pl.addError("Invalid", "Floor declaration is not satisfying the rule");
									}
								}
								else
								{
									pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
								}
							}
							else
							{
								if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
									isAccepted = true;
									requiredFloorCount = "Maximum 2 Floors";
									actualFloorCount = floorAbvGround.toString() + " Floors";
								}
								else
								{
									pl.addError("Invalid", "Floor declaration is not satisfying the rule");
								}
							}
						}
						else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
								|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)
									|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
							if (ewsPlot) {
								if(ewsBuilding) {
									if (basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
										isAccepted = true;
										requiredFloorCount = "Stilt/Basement + 2 Floors";
										actualFloorCount = "Basement + " + floorAbvGround.toString() + " Floors";
									}
									else if (!basementFloor && stiltFloor && floorAbvGround.compareTo(THREE) <= 0) {
										isAccepted = true;
										requiredFloorCount = "Stilt/Basement + 2 Floors";
										actualFloorCount = "Stilt + " + floorAbvGround.subtract(ONE).toString() + " Floors";
									}
									else if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
										isAccepted = true;
										requiredFloorCount = "Stilt/Basement + 2 Floors";
										actualFloorCount = floorAbvGround.toString() + " Floors";
									}
									else
									{
										pl.addError("Invalid", "Floor declaration is not satisfying the rule");
									}
								}
								else
								{
									pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
								}
							}
							else
							{
								if (basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
									isAccepted = true;
									requiredFloorCount = "Stilt/Basement + 2 Floors";
									actualFloorCount = "Basement + " + floorAbvGround.toString() + " Floors";
								}
								else if (!basementFloor && stiltFloor && floorAbvGround.compareTo(THREE) <= 0) {
									isAccepted = true;
									requiredFloorCount = "Stilt/Basement + 2 Floors";
									actualFloorCount = "Stilt + " + floorAbvGround.subtract(ONE).toString() + " Floors";
								}
								else if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
									isAccepted = true;
									requiredFloorCount = "Stilt/Basement + 2 Floors";
									actualFloorCount = floorAbvGround.toString() + " Floors";
								}
								else
								{
									pl.addError("Invalid", "Floor declaration is not satisfying the rule");
								}
							}
						}
						else
						{
							pl.addError("Invalid", "Invalid classification of area type is defined in Plan Information");
						}
						break;
					case DxfFileConstants_Pondicherry.CRZ3:
						if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)
								|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
							if (ewsPlot) {
								if(ewsBuilding) {
									if (basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
										isAccepted = true;
										requiredFloorCount = "Basement + 2 Floors";
										actualFloorCount = "Basement + " + floorAbvGround.toString() + " Floors";
									}
									else if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
										isAccepted = true;
										requiredFloorCount = "Basement + 2 Floors";
										actualFloorCount = floorAbvGround.toString() + " Floors";
									}
									else
									{
										pl.addError("Invalid", "Floor declaration is not satisfying the rule");
									}
								}
								else
								{
									pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
								}
							}
							else
							{
								if (basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
									isAccepted = true;
									requiredFloorCount = "Basement + 2 Floors";
									actualFloorCount = "Basement + " + floorAbvGround.toString() + " Floors";
								}
								else if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
									isAccepted = true;
									requiredFloorCount = "Basement + 2 Floors";
									actualFloorCount = floorAbvGround.toString() + " Floors";
								}
								else
								{
									pl.addError("Invalid", "Floor declaration is not satisfying the rule");
								}
							}
						}
						else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN) 
								|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)) {
							pl.addError("Invalid", "CRZ-III is not applicable for White Town and Tamil Town");
						}
						else
						{
							pl.addError("Invalid", "Invalid classification of area type is defined in Plan Information");
						}
						break;
					default:
						pl.addError("Invalid", "Invalid CRZ is defined in Plan Information");
						break;
				}
			}
			else
			{
				if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
						|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)
							|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
					if (ewsPlot) {
						if(ewsBuilding) {
							if (basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
								isAccepted = true;
								requiredFloorCount = "Stilt/Basement + 2 Floors";
								actualFloorCount = "Basement + " + floorAbvGround.toString() + " Floors";
							}
							else if (!basementFloor && stiltFloor && floorAbvGround.compareTo(THREE) <= 0) {
								isAccepted = true;
								requiredFloorCount = "Stilt/Basement + 2 Floors";
								actualFloorCount = "Stilt + " + floorAbvGround.subtract(ONE).toString() + " Floors";
							}
							else if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
								isAccepted = true;
								requiredFloorCount = "Stilt/Basement + 2 Floors";
								actualFloorCount = floorAbvGround.toString() + " Floors";
							}
							else
							{
								pl.addError("Invalid", "Floor declaration is not satisfying the rule");
							}
						}
						else
						{
							pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
						}
					}
					else
					{
						if (basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
							isAccepted = true;
							requiredFloorCount = "Stilt/Basement + 2 Floors";
							actualFloorCount = "Basement + " + floorAbvGround.toString() + " Floors";
						}
						else if (!basementFloor && stiltFloor && floorAbvGround.compareTo(THREE) <= 0) {
							isAccepted = true;
							requiredFloorCount = "Stilt/Basement + 2 Floors";
							actualFloorCount = "Stilt + " + floorAbvGround.subtract(ONE).toString() + " Floors";
						}
						else if (!basementFloor && !stiltFloor && floorAbvGround.compareTo(TWO) <= 0) {
							isAccepted = true;
							requiredFloorCount = "Stilt/Basement + 2 Floors";
							actualFloorCount = floorAbvGround.toString() + " Floors";
						}
						else
						{
							pl.addError("Invalid", "Floor declaration is not satisfying the rule");
						}
					}
				}
				else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)) {
    				pl.addError("Invalid", "Regular Building is not applicable for White Town");
				}
				else
				{
					pl.addError("Invalid", "Invalid classification of area type is defined in Plan Information");
				}
			}
			
			if (errors.isEmpty() && StringUtils.isNotBlank(requiredFloorCount)) {
				Map<String, String> details = new HashMap<String, String>();
				details.put(RULE_NO, RULE);
				details.put(DESCRIPTION, NO_OF_FLOORS);
				details.put(PERMISSIBLE, requiredFloorCount);
				details.put(PROVIDED, actualFloorCount);
				details.put(STATUS, isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
				scrutinyDetail.getDetail().add(details);
				pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
			} 
		}
	}
  
	public Boolean isEwsPlot(Plan pl) {
		if (pl.getPlanInformation().getPlotArea().compareTo(BigDecimal.valueOf(100l)) < 0)
			return true;
		else
			return false;
	}
  
	public Boolean isEwsBuilding(Plan pl) {
		if(StringUtils.isNotBlank(pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.EWS_BUILDING)) 
				&& pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.EWS_BUILDING).equalsIgnoreCase(DcrConstants.YES)) 
			return true;
		else 
			return false;
	}
  
	public Boolean isStiltFloor(Plan pl) {
		if(StringUtils.isNotBlank(pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.STILT_FLOOR)) 
				&& pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.STILT_FLOOR).equalsIgnoreCase(DcrConstants.YES)) 
			return true;
		else 
			return false;
	}
  
	public Boolean isBasementFloor(Block block) {
		boolean isBasement = false;
		for (Floor floor : block.getBuilding().getFloors())
		{
			System.out.println("floor.getNumber().intValue()"+floor.getNumber().intValue());
			if (floor.getNumber().intValue() == -1)
			{
				isBasement = true;
			}
		}
		return isBasement;
	}
  
	private ScrutinyDetail getNewScrutinyDetail(String key) {
		ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.addColumnHeading(Integer.valueOf(1), RULE_NO);
		scrutinyDetail.addColumnHeading(Integer.valueOf(2), DESCRIPTION);
		scrutinyDetail.addColumnHeading(Integer.valueOf(3), PERMISSIBLE);
		scrutinyDetail.addColumnHeading(Integer.valueOf(4), PROVIDED);
		scrutinyDetail.addColumnHeading(Integer.valueOf(5), STATUS);
		scrutinyDetail.setKey(key);
		return scrutinyDetail;
	}
  
	public Map<String, Date> getAmendments() { 
		return new LinkedHashMap(); 
	}
}
