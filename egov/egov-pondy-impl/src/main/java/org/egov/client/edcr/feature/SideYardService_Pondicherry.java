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
import org.egov.common.entity.edcr.Yard;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SideYardService_Pondicherry extends GeneralRule
{
  private static final Logger LOG = Logger.getLogger(SideYardService_Pondicherry.class);
  private static final BigDecimal SIDEVALUE_ONE = BigDecimal.valueOf(1L);
  private static final BigDecimal SIDEVALUE_ONEPOINTFIVE = BigDecimal.valueOf(1.5D);
  private static final BigDecimal SIDEVALUE_THREE = BigDecimal.valueOf(3L);
  private static final String RULE = "Side Setback";
  private static final String RULE_PART_TWO_TABLE_ONE = "Part-II Table-1";
  private static final String SIDE_YARD_2_NOTDEFINED = "side2yardNodeDefined";
  private static final String SIDE_YARD_1_NOTDEFINED = "side1yardNodeDefined";
  public static final String BSMT_SIDE_YARD_DESC = "Basement Side Yard";
  public static final BigDecimal ROAD_WIDTH_TWELVE_POINTTWO = BigDecimal.valueOf(12.2D);

  private class SideYardResult
  {
    String rule;
    String subRule;
    String blockName;
    Integer level;
    String occupancy;
    BigDecimal actualDistance = BigDecimal.ZERO;
    BigDecimal actualMeanDistance = BigDecimal.ZERO;
    BigDecimal expectedDistance = BigDecimal.ZERO;
    BigDecimal expectedmeanDistance = BigDecimal.ZERO;
    boolean status = false;
    private SideYardResult() {
    }
  }
  
  public void processSideYard(Plan pl) {
    HashMap<String, String> errors = new HashMap<String, String>();
    Plot plot = pl.getPlot();
    if (plot == null) {
      return;
    }
    validateSideYardRule(pl);

    Boolean valid = Boolean.valueOf(false);
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
        this.scrutinyDetail.setHeading("Side Setback");
        SideYardResult sideYard1Result = new SideYardResult();
        SideYardResult sideYard2Result = new SideYardResult();
        
        for (SetBack setback : block.getSetBacks()) {
          Yard sideYard1 = null;
          Yard sideYard2 = null;
          
          if (setback.getSideYard1() != null && setback.getSideYard1().getMean().compareTo(BigDecimal.ZERO) > 0) {
            sideYard1 = setback.getSideYard1();
          }
          if (setback.getSideYard2() != null && setback.getSideYard2().getMean().compareTo(BigDecimal.ZERO) > 0) {
            sideYard2 = setback.getSideYard2();
          }

          if (sideYard1 != null || sideYard2 != null) {
            BigDecimal buildingHeight;
            
            if (sideYard1 != null && sideYard1.getHeight() != null && sideYard1.getHeight().compareTo(BigDecimal.ZERO) > 0 
            		&& sideYard2 != null && sideYard2.getHeight() != null && sideYard2.getHeight().compareTo(BigDecimal.ZERO) > 0) {
              buildingHeight = (sideYard1.getHeight().compareTo(sideYard2.getHeight()) >= 0) ? sideYard1.getHeight() : sideYard2.getHeight();
            }
            else {
              buildingHeight = (sideYard1 != null && sideYard1.getHeight() != null && sideYard1.getHeight().compareTo(BigDecimal.ZERO) > 0) ? sideYard1.getHeight() : ((sideYard2 != null && sideYard2.getHeight() != null && sideYard2.getHeight().compareTo(BigDecimal.ZERO) > 0) ? sideYard2.getHeight() : block.getBuilding().getBuildingHeight());
            } 
            
            double minlength = 0.0D;
            double max = 0.0D;
            double minMeanlength = 0.0D;
            double maxMeanLength = 0.0D;
            if (sideYard2 != null && sideYard1 != null) {
              if (sideYard2.getMinimumDistance().doubleValue() > sideYard1.getMinimumDistance().doubleValue()) {
                minlength = sideYard1.getMinimumDistance().doubleValue();
                max = sideYard2.getMinimumDistance().doubleValue();
              } else {
                minlength = sideYard2.getMinimumDistance().doubleValue();
                max = sideYard1.getMinimumDistance().doubleValue();
              }
            } else if (sideYard1 != null) {
              max = sideYard1.getMinimumDistance().doubleValue();
            } else {
              minlength = sideYard2.getMinimumDistance().doubleValue();
            } 
            
            if (buildingHeight != null && (minlength > 0.0D || max > 0.0D)) {
              for (Occupancy occupancy : block.getBuilding().getTotalArea()) {
                this.scrutinyDetail.setKey("Block_" + block.getName() + "_Side Setback");
                
                if (setback.getLevel().intValue() < 0) {
                  this.scrutinyDetail.setKey("Block_" + block.getName() + "_Basement Side Yard");
                  
                  checkSideYardBasement(pl, block.getBuilding(), buildingHeight, block.getName(), setback.getLevel(), plot, 
                		  minlength, max, minMeanlength, maxMeanLength, occupancy.getTypeHelper(), sideYard1Result, sideYard2Result);
                }
                
                if ((occupancy.getTypeHelper().getSubtype() != null && ("A-R".equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode()) 
                		|| "A-AF".equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode()) 
                		|| "A-PO".equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode()))) 
                		|| "F".equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) {
                    checkSideYard(pl, block.getBuilding(), buildingHeight, block.getName(), setback.getLevel(),	plot, 
                    		minlength, max, minMeanlength, maxMeanLength, occupancy.getTypeHelper(), sideYard1Result, sideYard2Result); 
                    continue;
                }
              }
              addSideYardResult(pl, errors, sideYard1Result, sideYard2Result);
            } 
            
            if (pl.getPlanInformation() != null && pl.getPlanInformation().getWidthOfPlot().compareTo(BigDecimal.valueOf(10L)) <= 0)
              exemptSideYardForAAndF(pl, block, sideYard1Result, sideYard2Result); 
            continue;
          } 
          if (pl.getPlanInformation() != null && pl.getPlanInformation().getWidthOfPlot().compareTo(BigDecimal.valueOf(10L)) <= 0) {
            exemptSideYardForAAndF(pl, block, sideYard1Result, sideYard2Result);
            addSideYardResult(pl, errors, sideYard1Result, sideYard2Result);
          } 
        } 
      } 
    }
  }
  
  private void addSideYardResult(Plan pl, HashMap<String, String> errors, SideYardResult sideYard1Result, SideYardResult sideYard2Result) {
    if (errors.isEmpty() && sideYard1Result != null) {
      Map<String, String> details = new HashMap<String, String>();
      details.put("Byelaw", sideYard1Result.subRule);
      details.put("Level", (sideYard1Result.level != null) ? sideYard1Result.level.toString() : "");
      details.put("Occupancy", sideYard1Result.occupancy);
      details.put("Field Verified", "Minimum distance ");
      details.put("Permissible", sideYard1Result.expectedDistance.toString());
      details.put("Provided", sideYard1Result.actualDistance.toString());
      details.put("Side Number", "Side Setback 1");
      
      if (sideYard1Result.status && errors.isEmpty()) {
        details.put("Status", Result.Accepted.getResultVal());
      } 
      else {
        details.put("Status", Result.Not_Accepted.getResultVal());
      } 
      
      this.scrutinyDetail.getDetail().add(details);
      pl.getReportOutput().getScrutinyDetails().add(this.scrutinyDetail);
    } 
    
    if (errors.isEmpty() && sideYard2Result != null) {
      Map<String, String> detailsSideYard2 = new HashMap<String, String>();
      detailsSideYard2.put("Byelaw", sideYard2Result.subRule);
      detailsSideYard2.put("Level", (sideYard2Result.level != null) ? sideYard2Result.level.toString() : "");
      detailsSideYard2.put("Occupancy", sideYard2Result.occupancy);
      detailsSideYard2.put("Side Number", "Side Setback 2");
      detailsSideYard2.put("Field Verified", "Minimum distance ");
      detailsSideYard2.put("Permissible", sideYard2Result.expectedDistance.toString());
      detailsSideYard2.put("Provided", sideYard2Result.actualDistance.toString());
      
      if (sideYard2Result.status && errors.isEmpty()) {
        detailsSideYard2.put("Status", Result.Accepted.getResultVal());
      } 
      else {
        detailsSideYard2.put("Status", Result.Not_Accepted.getResultVal());
      } 
      
      this.scrutinyDetail.getDetail().add(detailsSideYard2);
      pl.getReportOutput().getScrutinyDetails().add(this.scrutinyDetail);
    } 
  }
  
  private void exemptSideYardForAAndF(Plan pl, Block block, SideYardResult sideYard1Result, SideYardResult sideYard2Result) {
	  for (Occupancy occupancy : block.getBuilding().getTotalArea()) {
		  this.scrutinyDetail.setKey("Block_" + block.getName() + "_Side Setback");
		  if ((occupancy.getTypeHelper().getType() != null && "A".equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) 
    		  || "F".equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) {
			  if (pl.getErrors().containsKey(SIDE_YARD_2_NOTDEFINED)) {
				  pl.getErrors().remove(SIDE_YARD_2_NOTDEFINED);
			  }
			  if (pl.getErrors().containsKey(SIDE_YARD_1_NOTDEFINED)) {
		          pl.getErrors().remove(SIDE_YARD_1_NOTDEFINED);
		      }
			  if (pl.getErrors().containsKey("Side Setback")) {
		          pl.getErrors().remove("Side Setback");
		      }
			  if (pl.getErrors().containsValue("BLK_" + block.getNumber() + "_LVL_0_SIDE_SETBACK1 not defined in the plan.")) {
		          pl.getErrors().remove("", "BLK_" + block.getNumber() + "_LVL_0_SIDE_SETBACK1 not defined in the plan.");
		      }
			  if (pl.getErrors().containsValue("BLK_" + block.getNumber() + "_LVL_0_SIDE_SETBACK2 not defined in the plan.")) {
		          pl.getErrors().remove("", "BLK_" + block.getNumber() + "_LVL_0_SIDE_SETBACK2 not defined in the plan.");
		      }
		  }
		  
		  compareSideYard2Result(block.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
				  occupancy.getTypeHelper(), sideYard2Result, Boolean.valueOf(true), "Part-II Table-1", "Side Setback", Integer.valueOf(0));
		  compareSideYard1Result(block.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
				  occupancy.getTypeHelper(), sideYard1Result, Boolean.valueOf(true), "Part-II Table-1", "Side Setback", Integer.valueOf(0));
    } 
  }
  
  
  private void checkSideYard(Plan pl, Building building, BigDecimal buildingHeight, String blockName, Integer level, Plot plot, 
		  double min, double max, double minMeanlength, double maxMeanLength, OccupancyTypeHelper mostRestrictiveOccupancy, 
		  SideYardResult sideYard1Result, SideYardResult sideYard2Result) {
    String rule = "Side Setback";
    String subRule = "Part-II Table-1";
    Boolean valid2 = Boolean.valueOf(false);
    Boolean valid1 = Boolean.valueOf(false);
    BigDecimal side2val = BigDecimal.ZERO;
    BigDecimal side1val = BigDecimal.ZERO;
    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
    String typeOfArea = pl.getPlanInformation().getTypeOfArea();
    
    if (mostRestrictiveOccupancy.getSubtype() != null && ("A-R".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()) 
    		|| "A-AF".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()) || "A-PO".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))) {
      if (pl.getPlanInformation() != null && StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone()) 
    		  && "RESIDENTIAL".equalsIgnoreCase(pl.getPlanInformation().getLandUseZone()) && pl.getPlanInformation().getDepthOfPlot() != null) {
        checkSideYardForResidential(pl, blockName, level, min, max, minMeanlength, maxMeanLength, mostRestrictiveOccupancy, 
        		sideYard1Result, sideYard2Result, rule, subRule, valid2, valid1, side2val, side1val, typeOfArea, depthOfPlot);
      }
    }
  }
  
  private void checkSideYardForResidential(Plan pl, String blockName, Integer level, double min, double max, double minMeanlength, 
		  double maxMeanLength, OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result, SideYardResult sideYard2Result, 
		  String rule, String subRule, Boolean valid2, Boolean valid1, BigDecimal side2val, BigDecimal side1val, String typeOfArea, BigDecimal depthOfPlot) {
	    String crz = pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.CRZ_AREA);
		String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
		Boolean ewsBuilding = isEwsBuilding(pl);
		Boolean ewsPlot = isEwsPlot(pl);
		Boolean CRZZone = false;
	
		LOG.info("CRZ="+pl.getPlanInformation().getCrzZoneArea());
		if(crzValue!=null && crzValue.equalsIgnoreCase(DcrConstants.YES))
		{
			CRZZone=true;
		}
		
		if (CRZZone) {
			switch (crz) {
				case DxfFileConstants_Pondicherry.CRZ2:
					if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN) 
							|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
								|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)) {
						if (ewsPlot) {
							if(ewsBuilding) {
							    side1val = BigDecimal.ZERO;
							    side2val = BigDecimal.ZERO;
								subRule = DcrConstants.NA;
							}
							else
							{
								pl.addError("Invalid", "Regular Building not allowed in EWS plot (Side Set Back)");
							}
						}
						else
						{
							side1val = BigDecimal.ZERO;
							side2val = BigDecimal.ZERO;
							subRule = DcrConstants.NA;
						}
					}
					else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA))
					{
						if (ewsPlot) {
							if(ewsBuilding) {
								side1val = BigDecimal.ZERO;
							    side2val = BigDecimal.ZERO;
								subRule = RULE_PART_TWO_TABLE_ONE;
							}
							else
							{
								pl.addError("Invalid", "Regular Building not allowed in EWS plot (Side Set Back)");
							}
						}
						else
						{
							if (depthOfPlot == null) {
								if (pl.getErrors().containsKey(SIDE_YARD_2_NOTDEFINED)) {
									pl.getErrors().remove(SIDE_YARD_2_NOTDEFINED);
							    }
							    if (pl.getErrors().containsKey(SIDE_YARD_1_NOTDEFINED)) {
							        pl.getErrors().remove(SIDE_YARD_1_NOTDEFINED);
							    }
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(4.5D)) <= 0) {
								side1val = BigDecimal.ZERO;
							    side2val = BigDecimal.ZERO;
								subRule = RULE_PART_TWO_TABLE_ONE;
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(4.5D)) > 0 
							    	&& depthOfPlot.compareTo(BigDecimal.valueOf(6.1D)) <= 0) {
								side1val = SIDEVALUE_ONE;
							    side2val = BigDecimal.ZERO;
								subRule = RULE_PART_TWO_TABLE_ONE;
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(6.1D)) > 0 
							    	&& depthOfPlot.compareTo(BigDecimal.valueOf(9.15D)) <= 0) {
								side1val = SIDEVALUE_ONE;
							    side2val = SIDEVALUE_ONE;
								subRule = RULE_PART_TWO_TABLE_ONE;
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(9.15D)) > 0) {
							    side1val = SIDEVALUE_ONEPOINTFIVE;
							    side2val = SIDEVALUE_ONEPOINTFIVE;
							    subRule = RULE_PART_TWO_TABLE_ONE;
							}
						}
					}
					else
					{
						pl.addError("Invalid", "Invalid classification of area type is defined (Side Set Back)");
					}
					break;
				case DxfFileConstants_Pondicherry.CRZ3:
					if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN) 
							|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
								|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)) {
						if (ewsPlot) {
							if(ewsBuilding) {
								side1val = BigDecimal.ZERO;
							    side2val = BigDecimal.ZERO;
								subRule = DcrConstants.NA;
							}
							else
							{
								pl.addError("Invalid", "Regular Building not allowed in EWS plot (Side Set Back)");
							}
						}
						else
						{
							side1val = BigDecimal.ZERO;
							side2val = BigDecimal.ZERO;
							subRule = DcrConstants.NA;
						}
					}
					else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA))
					{
						if (ewsPlot) {
							if(ewsBuilding) {
								side1val = BigDecimal.ZERO;
							    side2val = BigDecimal.ZERO;
								subRule = RULE_PART_TWO_TABLE_ONE;
							}
							else
							{
								pl.addError("Invalid", "Regular Building not allowed in EWS plot (Side Set Back)");
							}
						}
						else
						{
							if (depthOfPlot == null) {
								if (pl.getErrors().containsKey(SIDE_YARD_2_NOTDEFINED)) {
									pl.getErrors().remove(SIDE_YARD_2_NOTDEFINED);
							    }
							    if (pl.getErrors().containsKey(SIDE_YARD_1_NOTDEFINED)) {
							        pl.getErrors().remove(SIDE_YARD_1_NOTDEFINED);
							    }
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(4.5D)) <= 0) {
								side1val = BigDecimal.ZERO;
							    side2val = BigDecimal.ZERO;
								subRule = RULE_PART_TWO_TABLE_ONE;
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(4.5D)) > 0 
							    	&& depthOfPlot.compareTo(BigDecimal.valueOf(6.1D)) <= 0) {
								side1val = SIDEVALUE_ONE;
							    side2val = BigDecimal.ZERO;
								subRule = RULE_PART_TWO_TABLE_ONE;
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(6.1D)) > 0 
							    	&& depthOfPlot.compareTo(BigDecimal.valueOf(9.15D)) <= 0) {
								side1val = SIDEVALUE_ONE;
							    side2val = SIDEVALUE_ONE;
								subRule = RULE_PART_TWO_TABLE_ONE;
							} else if (depthOfPlot.compareTo(BigDecimal.valueOf(9.15D)) > 0) {
							    side1val = SIDEVALUE_ONEPOINTFIVE;
							    side2val = SIDEVALUE_ONEPOINTFIVE;
							    subRule = RULE_PART_TWO_TABLE_ONE;
							}
						}
					}
					else
					{
						pl.addError("Invalid", "Invalid classification of area type is defined (Side Set Back)");
					}
					break;
				default:
					pl.addError("Invalid", "Invalid CRZ is defined (Side Set Back)");
					break;
			}
		}
		else
		{
			if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN) 
					|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
						|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)) {
				if (ewsPlot) {
					if(ewsBuilding) {
					    side1val = BigDecimal.ZERO;
					    side2val = BigDecimal.ZERO;
						subRule = DcrConstants.NA;
					}
					else
					{
						pl.addError("Invalid", "Regular Building not allowed in EWS plot (Side Set Back)");
					}
				}
				else
				{
					side1val = BigDecimal.ZERO;
					side2val = BigDecimal.ZERO;
					subRule = DcrConstants.NA;
				}
			}
			else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA))
			{
				if (ewsPlot) {
					if(ewsBuilding) {
						side1val = BigDecimal.ZERO;
					    side2val = BigDecimal.ZERO;
						subRule = RULE_PART_TWO_TABLE_ONE;
					}
					else
					{
						pl.addError("Invalid", "Regular Building not allowed in EWS plot (Side Set Back)");
					}
				}
				else
				{
					if (depthOfPlot == null) {
						if (pl.getErrors().containsKey(SIDE_YARD_2_NOTDEFINED)) {
							pl.getErrors().remove(SIDE_YARD_2_NOTDEFINED);
					    }
					    if (pl.getErrors().containsKey(SIDE_YARD_1_NOTDEFINED)) {
					        pl.getErrors().remove(SIDE_YARD_1_NOTDEFINED);
					    }
					} else if (depthOfPlot.compareTo(BigDecimal.valueOf(4.5D)) <= 0) {
						side1val = BigDecimal.ZERO;
					    side2val = BigDecimal.ZERO;
						subRule = RULE_PART_TWO_TABLE_ONE;
					} else if (depthOfPlot.compareTo(BigDecimal.valueOf(4.5D)) > 0 
					    	&& depthOfPlot.compareTo(BigDecimal.valueOf(6.1D)) <= 0) {
						side1val = SIDEVALUE_ONE;
					    side2val = BigDecimal.ZERO;
						subRule = RULE_PART_TWO_TABLE_ONE;
					} else if (depthOfPlot.compareTo(BigDecimal.valueOf(6.1D)) > 0 
					    	&& depthOfPlot.compareTo(BigDecimal.valueOf(9.15D)) <= 0) {
						side1val = SIDEVALUE_ONE;
					    side2val = SIDEVALUE_ONE;
						subRule = RULE_PART_TWO_TABLE_ONE;
					} else if (depthOfPlot.compareTo(BigDecimal.valueOf(9.15D)) > 0) {
					    side1val = SIDEVALUE_ONEPOINTFIVE;
					    side2val = SIDEVALUE_ONEPOINTFIVE;
					    subRule = RULE_PART_TWO_TABLE_ONE;
					}
				}
			}
			else
			{
				pl.addError("Invalid", "Invalid classification of area type is defined (Side Set Back)");
			}
		}
    
    if (max >= side1val.doubleValue())
      valid1 = Boolean.valueOf(true); 
    if (min >= side2val.doubleValue()) {
      valid2 = Boolean.valueOf(true);
    }
    compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO, 
        BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule, level);
    
    compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO, 
        BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule, level);
  }

  public Boolean isEwsPlot(Plan pl) {
	  if(pl.getPlanInformation().getPlotArea().compareTo(BigDecimal.valueOf(100l)) < 0)
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
	
  private void checkSideYardBasement(Plan pl, Building building, BigDecimal buildingHeight, String blockName, Integer level, 
		  Plot plot, double min, double max, double minMeanlength, double maxMeanLength, OccupancyTypeHelper mostRestrictiveOccupancy, 
		  SideYardResult sideYard1Result, SideYardResult sideYard2Result) {
    String rule = "Basement Side Yard";
    String subRule = "47";
    Boolean valid2 = Boolean.valueOf(false);
    Boolean valid1 = Boolean.valueOf(false);
    BigDecimal side2val = BigDecimal.ZERO;
    BigDecimal side1val = BigDecimal.ZERO;
    
    if (((mostRestrictiveOccupancy.getSubtype() != null && "A-R".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())) 
    		|| "A-PO".equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()) || "F".equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) 
    		&& plot.getArea().compareTo(BigDecimal.valueOf(300L)) <= 0) {
      side2val = SIDEVALUE_THREE;
      side1val = SIDEVALUE_THREE;
      
      if (max >= side1val.doubleValue())
        valid1 = Boolean.valueOf(true); 
      if (min >= side2val.doubleValue()) {
        valid2 = Boolean.valueOf(true);
      }
      
      compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO, 
          BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule, level);
      
      compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO, 
          BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule, level);
    } 
  }

  private void compareSideYard1Result(String blockName, BigDecimal exptDistance, BigDecimal actualDistance, 
		  BigDecimal expectedMeanDistance, BigDecimal actualMeanDistance, OccupancyTypeHelper mostRestrictiveOccupancy, 
		  SideYardResult sideYard1Result, Boolean valid, String subRule, String rule, Integer level) {
    String occupancyName;
    if (mostRestrictiveOccupancy.getSubtype() != null) {
      occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
    } else {
      occupancyName = mostRestrictiveOccupancy.getType().getName();
    }  
    if (exptDistance.compareTo(sideYard1Result.expectedDistance) >= 0) {
      if (exptDistance.compareTo(sideYard1Result.expectedDistance) == 0) {
        sideYard1Result.rule = (sideYard1Result.rule != null) ? (sideYard1Result.rule + "," + rule) : rule;
        sideYard1Result.occupancy = (sideYard1Result.occupancy != null) ? (sideYard1Result.occupancy + "," + occupancyName) : occupancyName;
      }
      else {
        sideYard1Result.rule = rule;
        sideYard1Result.occupancy = occupancyName;
      } 
      
      sideYard1Result.subRule = subRule;
      sideYard1Result.blockName = blockName;
      sideYard1Result.level = level;
      sideYard1Result.actualDistance = actualDistance;
      sideYard1Result.expectedDistance = exptDistance;
      sideYard1Result.status = valid.booleanValue();
    } 
  }

  private void compareSideYard2Result(String blockName, BigDecimal exptDistance, BigDecimal actualDistance, 
		  BigDecimal expectedMeanDistance, BigDecimal actualMeanDistance, OccupancyTypeHelper mostRestrictiveOccupancy, 
		  SideYardResult sideYard2Result, Boolean valid, String subRule, String rule, Integer level) {
    String occupancyName;
    if (mostRestrictiveOccupancy.getSubtype() != null) {
      occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
    } else {
      occupancyName = mostRestrictiveOccupancy.getType().getName();
    }  
    if (exptDistance.compareTo(sideYard2Result.expectedDistance) >= 0) {
      if (exptDistance.compareTo(sideYard2Result.expectedDistance) == 0) {
        sideYard2Result.rule = (sideYard2Result.rule != null) ? (sideYard2Result.rule + "," + rule) : rule;
        sideYard2Result.occupancy = (sideYard2Result.occupancy != null) ? (sideYard2Result.occupancy + "," + occupancyName) : occupancyName;
      }
      else {
        sideYard2Result.rule = rule;
        sideYard2Result.occupancy = occupancyName;
      } 
      
      sideYard2Result.subRule = subRule;
      sideYard2Result.blockName = blockName;
      sideYard2Result.level = level;
      sideYard2Result.actualDistance = actualDistance;
      sideYard2Result.expectedDistance = exptDistance;
      sideYard2Result.status = valid.booleanValue();
    } 
  }

  private void validateSideYardRule(Plan pl) {
    for (Block block : pl.getBlocks()) {
      if (!block.getCompletelyExisting().booleanValue()) {
        Boolean sideYardDefined = Boolean.valueOf(false);
        for (SetBack setback : block.getSetBacks()) {
          if (setback.getSideYard1() != null && setback.getSideYard1().getMean().compareTo(BigDecimal.valueOf(0L)) > 0) {
            sideYardDefined = Boolean.valueOf(true); continue;
          }  if (setback.getSideYard2() != null && setback.getSideYard2().getMean().compareTo(BigDecimal.valueOf(0L)) > 0) {
            sideYardDefined = Boolean.valueOf(true);
          }
        } 
        if (!sideYardDefined.booleanValue()) {
          HashMap<String, String> errors = new HashMap<String, String>();
          errors.put("Side Setback", 
              prepareMessage("msg.error.not.defined", new String[] { "Side Setback for Block " + block.getName() }));
          pl.addErrors(errors);
        } 
      } 
    } 
  }
}
