package org.egov.client.edcr;

import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_R;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
import org.egov.edcr.feature.FrontYardService;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.stereotype.Service;

@Service
public class FrontYardService_pondicherry extends FrontYardService {
  
  private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_NIL = BigDecimal.valueOf(0);
  
  private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1 = BigDecimal.valueOf(1D);
  
  private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1_5 = BigDecimal.valueOf(1.5D);
  
  private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_3 = BigDecimal.valueOf(3L);
  
  private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_4_5 = BigDecimal.valueOf(4.5D);
  
  private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_6 = BigDecimal.valueOf(6L);
  
  public static final BigDecimal ROAD_WIDTH_TWELVE_POINTTWO = BigDecimal.valueOf(12.2D);
  
  public static final String BSMT_FRONT_YARD_DESC = "Basement Front Yard";
  
  public static final String TAMIL_TOWN = "TAMIL TOWN";
	public static final String WHITE_TOWN = "WHITE TOWN";
	public static final String OTHER_AREA = "OTHER AREA";
	public static final String OUTSIDE_BOULEVARD = "OUTSIDE BOULEVARD";
	public static final String CRZ1 = "CRZ-I";
	public static final String CRZ2 = "CRZ-II";
	public static final String CRZ3 = "CRZ-III";
  
  
  private class FrontYardResult {
    String rule;
    
    String subRule;
    
    Integer level;
    
    private FrontYardResult() {}
    
    BigDecimal actualMinDistance = BigDecimal.ZERO;
    
    String occupancy;
    
    BigDecimal expectedminimumDistance = BigDecimal.ZERO;
    
    boolean status = false;
  }
  
@Override
 public void processFrontYard(Plan pl) {
    Plot plot = pl.getPlot();
    HashMap<String, String> errors = new HashMap<>();
    if (plot == null)
      return; 
    validateFrontYard(pl);
    if (plot != null && !pl.getBlocks().isEmpty())
      for (Block block : pl.getBlocks()) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(Integer.valueOf(1), "Byelaw");
        scrutinyDetail.addColumnHeading(Integer.valueOf(2), "Level");
        scrutinyDetail.addColumnHeading(Integer.valueOf(3), "Occupancy");
        scrutinyDetail.addColumnHeading(Integer.valueOf(4), "Field Verified");
        scrutinyDetail.addColumnHeading(Integer.valueOf(5), "Permissible");
        scrutinyDetail.addColumnHeading(Integer.valueOf(6), "Provided");
        scrutinyDetail.addColumnHeading(Integer.valueOf(7), "Status");
        scrutinyDetail.setHeading("Front Setback");
        FrontYardResult frontYardResult = new FrontYardResult();
        for (SetBack setback : block.getSetBacks()) {
          if (setback.getFrontYard() != null) {
            BigDecimal min = setback.getFrontYard().getMinimumDistance();
            BigDecimal mean = setback.getFrontYard().getMean();
            BigDecimal buildingHeight = (setback.getFrontYard().getHeight() != null && setback.getFrontYard().getHeight().compareTo(BigDecimal.ZERO) > 0) ? setback.getFrontYard().getHeight() : block.getBuilding().getBuildingHeight();
            if (buildingHeight != null && (min.doubleValue() > 0.0D || mean.doubleValue() > 0.0D)) {
              for (Occupancy occupancy : block.getBuilding().getTotalArea()) {
                scrutinyDetail.setKey("Block_" + block.getName() + "_" + "Front Setback");
                if (setback.getLevel().intValue() < 0) {
                  scrutinyDetail.setKey("Block_" + block.getName() + "_Basement Front Yard");
                  checkFrontYardBasement(pl, block.getBuilding(), block.getName(), setback.getLevel(), plot, "Basement Front Yard", min, mean, occupancy
                      .getTypeHelper(), frontYardResult);
                } 

                OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();
                if ((mostRestrictiveOccupancyType.getType() != null
    					&& DxfFileConstants.A.equalsIgnoreCase(mostRestrictiveOccupancyType.getType().getCode()))
    					|| (mostRestrictiveOccupancyType.getSubtype() != null
    							&& (A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())
    									|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())))) {  				
    				checkFrontYardResidential(pl, block.getBuilding(), block.getName(), setback
                          .getLevel(), plot, "Front Setback", min, mean, occupancy
                          .getTypeHelper(), frontYardResult, errors);
                      continue;
    			}
              } 

              
              if (errors.isEmpty()) {
                Map<String, String> details = new HashMap<>();
                details.put("Byelaw", frontYardResult.subRule);
                details.put("Level", (frontYardResult.level != null) ? frontYardResult.level
                    .toString() : "");
                details.put("Occupancy", frontYardResult.occupancy);
                details.put("Field Verified", "Minimum distance ");
                details.put("Permissible", frontYardResult.expectedminimumDistance.toString());
                details.put("Provided", frontYardResult.actualMinDistance.toString());
                if (frontYardResult.status) {
                  details.put("Status", Result.Accepted.getResultVal());
                } else {
                  details.put("Status", Result.Not_Accepted.getResultVal());
                } 
                scrutinyDetail.getDetail().add(details);
                pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
              } 
            } 
          } 
        } 
      }  
  }
  
  private void validateFrontYard(Plan pl) {
    for (Block block : pl.getBlocks()) {
      if (!block.getCompletelyExisting().booleanValue()) {
        Boolean frontYardDefined = Boolean.valueOf(false);
        for (SetBack setback : block.getSetBacks()) {
          if (setback.getFrontYard() != null && setback
            .getFrontYard().getMean().compareTo(BigDecimal.valueOf(0L)) > 0)
            frontYardDefined = Boolean.valueOf(true); 
        } 
        if (!frontYardDefined.booleanValue()) {
          HashMap<String, String> errors = new HashMap<>();
          errors.put("Front Setback", 
              prepareMessage("msg.error.not.defined", new String[] { "Front Setback for Block " + block.getName() }));
          pl.addErrors(errors);
        } 
      } 
    } 
  }
  
  private Boolean checkFrontYardResidential(Plan pl, Building building, String blockName, Integer level, Plot plot, String frontYardFieldName, BigDecimal min, BigDecimal mean, OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult, HashMap<String, String> errors) {
	    Boolean valid = Boolean.valueOf(false);
	    String subRule = "Part-II Table-1";
	    String rule = "Front Setback";
	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    BigDecimal roadWidth = pl.getPlanInformation().getRoadWidth();
	    String typeOfArea = pl.getPlanInformation().getTypeOfArea();
		// getting additoinal property crz_area
		String crz = pl.getPlanInfoProperties().get("CRZ_AREA");
		// check it is ews
		Boolean ewsPlot = isEwsPlot(pl);
		Boolean ewsBuilding = isEwsBuilding(pl);
		Boolean CRZZone=false;
		String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
		if(crzValue!=null && crzValue.equalsIgnoreCase(DcrConstants.YES))
		{
			CRZZone=true;
		} 

	    if (typeOfArea.equalsIgnoreCase(TAMIL_TOWN) || typeOfArea.equalsIgnoreCase(WHITE_TOWN)) {
	    	minVal=FRONTYARDMINIMUM_DISTANCE_NIL;
	    }
	    if (typeOfArea.equalsIgnoreCase(OUTSIDE_BOULEVARD)) {
	    	minVal = FRONTYARDMINIMUM_DISTANCE_1;
	    }
	    if (typeOfArea.equalsIgnoreCase(OTHER_AREA)) {
	    	if (CRZZone) {
				switch (crz) {
				case CRZ2:
					if (ewsPlot) {
						minVal = FRONTYARDMINIMUM_DISTANCE_1;
					} else if (ewsBuilding) {
						if (roadWidth.compareTo(BigDecimal.valueOf(10L)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_1_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(10L)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(15.25)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_3;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(15.25)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(30.5)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_4_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(30.5)) > 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_6;
					      }
					} else {
						if (roadWidth.compareTo(BigDecimal.valueOf(10L)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_1_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(10L)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(15.25)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_3;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(15.25)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(30.5)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_4_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(30.5)) > 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_6;
					      }
					}
					break;
				case CRZ3:
					
					if (ewsPlot) {
						minVal = FRONTYARDMINIMUM_DISTANCE_1;
					} else if (ewsBuilding) {
						if (roadWidth.compareTo(BigDecimal.valueOf(10L)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_1_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(10L)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(15.25)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_3;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(15.25)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(30.5)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_4_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(30.5)) > 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_6;
					      }
					} else {
						if (roadWidth.compareTo(BigDecimal.valueOf(10L)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_1_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(10L)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(15.25)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_3;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(15.25)) > 0 && roadWidth
					        .compareTo(BigDecimal.valueOf(30.5)) <= 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_4_5;
					      } else if (roadWidth.compareTo(BigDecimal.valueOf(30.5)) > 0) {
					        minVal = FRONTYARDMINIMUM_DISTANCE_6;
					      }
					}
					break;

				}
			} else {
				if (ewsPlot) {
					minVal = FRONTYARDMINIMUM_DISTANCE_1;
				} else if (ewsBuilding) {
					if (roadWidth.compareTo(BigDecimal.valueOf(10L)) <= 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_1_5;
				      } else if (roadWidth.compareTo(BigDecimal.valueOf(10L)) > 0 && roadWidth
				        .compareTo(BigDecimal.valueOf(15.25)) <= 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_3;
				      } else if (roadWidth.compareTo(BigDecimal.valueOf(15.25)) > 0 && roadWidth
				        .compareTo(BigDecimal.valueOf(30.5)) <= 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_4_5;
				      } else if (roadWidth.compareTo(BigDecimal.valueOf(30.5)) > 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_6;
				      }
				} else {
					if (roadWidth.compareTo(BigDecimal.valueOf(10L)) <= 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_1_5;
				      } else if (roadWidth.compareTo(BigDecimal.valueOf(10L)) > 0 && roadWidth
				        .compareTo(BigDecimal.valueOf(15.25)) <= 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_3;
				      } else if (roadWidth.compareTo(BigDecimal.valueOf(15.25)) > 0 && roadWidth
				        .compareTo(BigDecimal.valueOf(30.5)) <= 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_4_5;
				      } else if (roadWidth.compareTo(BigDecimal.valueOf(30.5)) > 0) {
				        minVal = FRONTYARDMINIMUM_DISTANCE_6;
				      }
				}
			}
	    } 
	      valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
	      compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule, minVal, meanVal, level);

	    return valid;
	  }
  
  private Boolean checkFrontYardBasement(Plan plan, Building building, String blockName, Integer level, Plot plot, String frontYardFieldName, BigDecimal min, BigDecimal mean, OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult) {
    Boolean valid = Boolean.valueOf(false);
    String subRule = "47";
    String rule = "Front Setback";
    BigDecimal minVal = BigDecimal.ZERO;
    BigDecimal meanVal = BigDecimal.ZERO;
    if ((mostRestrictiveOccupancy.getSubtype() != null && "A-R"
      .equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())) || "A-AF"
      .equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()) || "A-PO"
      .equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()) || "F"
      .equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
      if (plot.getArea().compareTo(BigDecimal.valueOf(300L)) <= 0) {
        minVal = FRONTYARDMINIMUM_DISTANCE_3;
        valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
      } 
      rule = "Basement Front Yard";
      compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule, minVal, meanVal, level);
    } 
    return valid;
  }
  
  private void compareFrontYardResult(String blockName, BigDecimal min, BigDecimal mean, OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult, Boolean valid, String subRule, String rule, BigDecimal minVal, BigDecimal meanVal, Integer level) {
    String occupancyName;
    if (mostRestrictiveOccupancy.getSubtype() != null) {
      occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
    } else {
      occupancyName = mostRestrictiveOccupancy.getType().getName();
    } 
    if (minVal.compareTo(frontYardResult.expectedminimumDistance) >= 0) {
      if (minVal.compareTo(frontYardResult.expectedminimumDistance) == 0) {
        frontYardResult.rule = (frontYardResult.rule != null) ? (frontYardResult.rule + "," + rule) : rule;
        frontYardResult.occupancy = (frontYardResult.occupancy != null) ? (frontYardResult.occupancy + "," + occupancyName) : occupancyName;
      } else {
        frontYardResult.rule = rule;
        frontYardResult.occupancy = occupancyName;
      } 
      frontYardResult.subRule = subRule;
      frontYardResult.level = level;
      frontYardResult.expectedminimumDistance = minVal;
      frontYardResult.actualMinDistance = min;
      frontYardResult.status = valid.booleanValue();
    } 
  }
  
  private Boolean validateMinimumAndMeanValue(BigDecimal min, BigDecimal mean, BigDecimal minval, BigDecimal meanval) {
    Boolean valid = Boolean.valueOf(false);
    if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0)
      valid = Boolean.valueOf(true); 
    return valid;
  }
  
	private Boolean isEwsBuilding(Plan pl) {
		if(pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(50l)) <= 0)
			return true;
		return false;
	}

	private Boolean isEwsPlot(Plan pl) {
		if(pl.getPlanInformation().getPlotArea().compareTo(BigDecimal.valueOf(100l)) < 0)
			return true;
		else return false;
	}
}
