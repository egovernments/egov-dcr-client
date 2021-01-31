package org.egov.client.edcr.feature;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.client.edcr.constants.DxfFileConstants_Pondicherry;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.FeatureProcess;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MezzanineFloorService_Pondicherry extends FeatureProcess
{
	private static final Logger LOG = Logger.getLogger(MezzanineFloorService_Pondicherry.class);
	private static final String FLOOR = "Floor";
	private static final String SUB_RULE = "Part-I Clause 35 & 31";
	private static final String RULE_DESC_MAX_AREA = "Maximum allowed area of mezzanine floor ";
	private static final String RULE_DESC_MIN_AREA = "Minimum area of mezzanine floor ";
	private static final String RULE_DESC_MIN_WIDTH= "Minimum width of mezzanine floor ";
	private static final String RULE_DESC_MIN_HEIGHT = "Minimum height of mezzanine floor ";
	private static final BigDecimal AREA_NINE_POINT_THREE = BigDecimal.valueOf(9.3D);
	private static final BigDecimal HEIGHT_TWO_POINT_TWO = BigDecimal.valueOf(2.2D);
	private static final BigDecimal WIDTH_TWO_POINT_FIVE = BigDecimal.valueOf(2.5D);
	
	public Plan validate(Plan pl) { 
		return pl;
	}
  
	public Plan process(Plan pl) {
		validate(pl);	
		if (pl != null && !pl.getBlocks().isEmpty()) {
			for (Block block : pl.getBlocks()) {
				this.scrutinyDetail = new ScrutinyDetail();
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(1), RULE_NO);
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(2), DESCRIPTION);
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(3), FLOOR);
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(4), PERMISSIBLE);
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(5), PROVIDED);
				this.scrutinyDetail.addColumnHeading(Integer.valueOf(6), STATUS);
				this.scrutinyDetail.setKey("Block_" + block.getNumber() + "_Mezzanine Floor");
				if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
					BigDecimal totalBuiltupArea = BigDecimal.ZERO;
					for (Floor floor : block.getBuilding().getFloors()) {
						BigDecimal builtupArea = BigDecimal.ZERO;
						for (Occupancy occ : floor.getOccupancies()) {
							if (!occ.getIsMezzanine().booleanValue() && occ.getBuiltUpArea() != null)
								builtupArea = builtupArea.add(occ.getBuiltUpArea().subtract(occ.getDeduction()));
						} 
						totalBuiltupArea = totalBuiltupArea.add(builtupArea);
						for (Occupancy mezzanineFloor : floor.getOccupancies()) {
							if (mezzanineFloor.getIsMezzanine().booleanValue() && floor.getNumber().intValue() != 0) {
								if (mezzanineFloor.getBuiltUpArea() != null && mezzanineFloor.getBuiltUpArea().doubleValue() > 0.0D 
										&& mezzanineFloor.getTypeHelper() == null) {
									pl.addError(" Not defined in the plan.", 
											getLocaleMessage("msg.error.mezz.occupancy.not.defined", new String[] { block.getNumber(), 
													String.valueOf(floor.getNumber()), mezzanineFloor.getMezzanineNumber() }));
								}
								BigDecimal mezzanineFloorArea = BigDecimal.ZERO;
								BigDecimal mezzanineFloorWidth = BigDecimal.ZERO;
								BigDecimal height = BigDecimal.ZERO;
								if (mezzanineFloor.getBuiltUpArea() != null)
								{
									mezzanineFloorArea = mezzanineFloor.getBuiltUpArea().subtract(mezzanineFloor.getDeduction());
								}
								
								boolean valid = false;
								BigDecimal oneThirdOfBuiltup = builtupArea.divide(BigDecimal.valueOf(3L), 2, DcrConstants.ROUNDMODE_MEASUREMENTS);
                
								if (mezzanineFloorArea.doubleValue() > 0.0D && mezzanineFloorArea.compareTo(oneThirdOfBuiltup) <= 0) {
									valid = true;
								}
								
								String floorNo = " floor " + floor.getNumber();
								mezzanineFloorWidth = mezzanineFloor.getWidth();
								height = mezzanineFloor.getHeight();
                
				                String crz = pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.CRZ_AREA);
				                String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
				                String typeOfArea = pl.getPlanInformation().getTypeOfArea();
				                Boolean ewsBuilding = isEwsBuilding(pl);
				                Boolean ewsPlot = isEwsPlot(pl);
				                Boolean CRZZone = false;

				                LOG.info("CRZ=" + pl.getPlanInformation().getCrzZoneArea());
				        		if (crzValue != null && crzValue.equalsIgnoreCase(DcrConstants.YES)) {
				        			CRZZone = true;
				        		}
        		
				        		if (CRZZone) {
				        			switch (crz) {
				        				case DxfFileConstants_Pondicherry.CRZ2:
				        					if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)
				        							|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
				        								|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)
				        									|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
				        						if (ewsPlot) {
				        							if(ewsBuilding) {        								
				        								if (mezzanineFloorWidth.compareTo(WIDTH_TWO_POINT_FIVE) >= 0) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
					      					            } 
					      								else {
					      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
					      											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
					      					            }
				        								
				        								if (mezzanineFloor.getBuiltUpArea().compareTo(AREA_NINE_POINT_THREE) >= 0) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Accepted.getResultVal());
				        								} 
				        								else {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Not_Accepted.getResultVal());
				        								}
				        								
				        								if (valid) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Accepted.getResultVal());
				        								}
				        								else {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Not_Accepted.getResultVal());
				        								}
				        								
				        								if (height.compareTo(HEIGHT_TWO_POINT_TWO) >= 0) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
					      					            } 
					      								else {
					      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
					      											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
					      					            }
				        							}
				        							else
				        							{
				        								pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
				        							}
				        						}
				        						else
				        						{
				        							if (mezzanineFloorWidth.compareTo(WIDTH_TWO_POINT_FIVE) >= 0) {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
				      					            } 
				      								else {
				      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
				      											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
				      					            }
				    								
				    								if (mezzanineFloor.getBuiltUpArea().compareTo(AREA_NINE_POINT_THREE) >= 0) {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Accepted.getResultVal());
				    								} 
				    								else {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Not_Accepted.getResultVal());
				    								}
				    								
				    								if (valid) {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Accepted.getResultVal());
				    								}
				    								else {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Not_Accepted.getResultVal());
				    								}
				    								
				    								if (height.compareTo(HEIGHT_TWO_POINT_TWO) >= 0) {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
				      					            } 
				      								else {
				      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
				      											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
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
				        								if (mezzanineFloorWidth.compareTo(WIDTH_TWO_POINT_FIVE) >= 0) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
					      					            } 
					      								else {
					      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
					      											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
					      					            }
				        								
				        								if (mezzanineFloor.getBuiltUpArea().compareTo(AREA_NINE_POINT_THREE) >= 0) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Accepted.getResultVal());
				        								} 
				        								else {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Not_Accepted.getResultVal());
				        								}
				        								
				        								if (valid) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Accepted.getResultVal());
				        								}
				        								else {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Not_Accepted.getResultVal());
				        								}
				        								
				        								if (height.compareTo(HEIGHT_TWO_POINT_TWO) >= 0) {
				        									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
				        											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
					      					            } 
					      								else {
					      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
					      											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
					      					            }
				        							}
				        							else
				        							{
				        								pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
				        							}
				        						}
				        						else
				        						{
				        							if (mezzanineFloorWidth.compareTo(WIDTH_TWO_POINT_FIVE) >= 0) {
				        								setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
				        										floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
				      					            } 
				      								else {
				      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
				      											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
				      					            }
				    								
				    								if (mezzanineFloor.getBuiltUpArea().compareTo(AREA_NINE_POINT_THREE) >= 0) {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Accepted.getResultVal());
				    								} 
				    								else {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Not_Accepted.getResultVal());
				    								}
				    								
				    								if (valid) {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Accepted.getResultVal());
				    								}
				    								else {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Not_Accepted.getResultVal());
				    								}
				    								
				    								if (height.compareTo(HEIGHT_TWO_POINT_TWO) >= 0) {
				    									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
				    											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
				      					            } 
				      								else {
				      									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
				      											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
				      					            }
				        						}
				        					}
				        					else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)
				        							|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN))
				        					{
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
												if (mezzanineFloorWidth.compareTo(WIDTH_TWO_POINT_FIVE) >= 0) {
													setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
															floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
				  					            } 
				  								else {
				  									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
				  											floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
				  					            }
												
												if (mezzanineFloor.getBuiltUpArea().compareTo(AREA_NINE_POINT_THREE) >= 0) {
													setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
															floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Accepted.getResultVal());
												} 
												else {
													setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
															floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Not_Accepted.getResultVal());
												}
												
												if (valid) {
													setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
															floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Accepted.getResultVal());
												}
												else {
													setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
															floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Not_Accepted.getResultVal());
												}
												
												if (height.compareTo(HEIGHT_TWO_POINT_TWO) >= 0) {
													setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
															floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
				  					            } 
				  								else {
				  									setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
				  											floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
				  					            }
											}
											else
											{
												pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
											}
										}
										else
										{
											if (mezzanineFloorWidth.compareTo(WIDTH_TWO_POINT_FIVE) >= 0) {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
														floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
											} 
											else {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_WIDTH + mezzanineFloor.getMezzanineNumber(), 
														floorNo, WIDTH_TWO_POINT_FIVE + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
											}
											
											if (mezzanineFloor.getBuiltUpArea().compareTo(AREA_NINE_POINT_THREE) >= 0) {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
														floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Accepted.getResultVal());
											} 
											else {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_AREA + mezzanineFloor.getMezzanineNumber(), 
														floorNo, AREA_NINE_POINT_THREE + "m2", mezzanineFloor.getBuiltUpArea() + "m2", Result.Not_Accepted.getResultVal());
											}
											
											if (valid) {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
														floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Accepted.getResultVal());
											}
											else {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MAX_AREA + mezzanineFloor.getMezzanineNumber(), 
														floorNo, oneThirdOfBuiltup + "m2", mezzanineFloorArea + "m2", Result.Not_Accepted.getResultVal());
											}
											
											if (height.compareTo(HEIGHT_TWO_POINT_TWO) >= 0) {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
														floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Accepted.getResultVal());
											} 
											else {
												setReportOutputDetails(pl, SUB_RULE, RULE_DESC_MIN_HEIGHT + mezzanineFloor.getMezzanineNumber(), 
														floorNo, HEIGHT_TWO_POINT_TWO + "(MTR)", height + "(MTR)", Result.Not_Accepted.getResultVal());
											}
										}
									}
									else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN))
									{
										pl.addError("Invalid", "Regular Building is not applicable for White Town");
									}
				        			else
				        			{
				        				pl.addError("Invalid", "Invalid classification of area type is defined in Plan Information");
				        			}
				        		}
							}
						}
					}
				}
			}
		}
		return pl;
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
	
	private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String floor, String expected, String actual, String status) {
		Map<String, String> details = new HashMap<String, String>();
		details.put(RULE_NO, ruleNo);
		details.put(DESCRIPTION, ruleDesc);
		details.put(FLOOR, floor);
		details.put(PERMISSIBLE, expected);
		details.put(PROVIDED, actual);
		details.put(STATUS, status);
		this.scrutinyDetail.getDetail().add(details);
		pl.getReportOutput().getScrutinyDetails().add(this.scrutinyDetail);
	}

	public Map<String, Date> getAmendments() {
		return new LinkedHashMap();
	}
}
