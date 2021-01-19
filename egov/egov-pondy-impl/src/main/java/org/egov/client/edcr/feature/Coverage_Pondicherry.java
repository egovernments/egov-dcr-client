package org.egov.client.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_R;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.client.edcr.constants.DxfFileConstants_Pondicherry;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.OccupancyType;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.Coverage;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.stereotype.Service;

@Service
public class Coverage_Pondicherry extends Coverage {
  private static final Logger LOG = Logger.getLogger(Coverage_Pondicherry.class);
  
  private static final String RULE_DESCRIPTION_KEY = "coverage.description";
  
  private static final String RULE_EXPECTED_KEY = "coverage.expected";
  
  private static final String RULE_ACTUAL_KEY = "coverage.actual";
  
  private static final BigDecimal FORTY = BigDecimal.valueOf(40L);
  
  public static final BigDecimal SEVENTY_FIVE = BigDecimal.valueOf(75L);
  
  public static final BigDecimal SEVENTY = BigDecimal.valueOf(70L);
  
  public static final BigDecimal EIGHTY = BigDecimal.valueOf(80L);
  
  public Plan validate(Plan pl) {
    for (Block block : pl.getBlocks()) {
      if (block.getCoverage().isEmpty())
        pl.addError("coverageArea" + block.getNumber(), "Coverage Area for block " + block.getNumber() + " not Provided"); 
    } 
    return pl;
  }
  
  public Plan process(Plan pl) {
	  LOG.info("Inside coverage process");
    validate(pl);
    BigDecimal totalCoverage = BigDecimal.ZERO;
    BigDecimal totalCoverageArea = BigDecimal.ZERO;
    BigDecimal expectedCoverage = BigDecimal.ZERO;
    for (Block block : pl.getBlocks()) {
      BigDecimal coverageAreaWithoutDeduction = BigDecimal.ZERO;
      BigDecimal coverageDeductionArea = BigDecimal.ZERO;
      for (Measurement coverage : block.getCoverage())
        coverageAreaWithoutDeduction = coverageAreaWithoutDeduction.add(coverage.getArea()); 
      for (Measurement deduct : block.getCoverageDeductions())
        coverageDeductionArea = coverageDeductionArea.add(deduct.getArea()); 
      if (block.getBuilding() != null) {
        block.getBuilding().setCoverageArea(coverageAreaWithoutDeduction.subtract(coverageDeductionArea));
        BigDecimal coverage = BigDecimal.ZERO;
        if (pl.getPlot().getArea().doubleValue() > 0.0D)
          coverage = block.getBuilding().getCoverageArea().multiply(BigDecimal.valueOf(100L)).divide(pl
              .getPlanInformation().getPlotArea(), 2, DcrConstants.ROUNDMODE_MEASUREMENTS); 
        block.getBuilding().setCoverage(coverage);
        totalCoverageArea = totalCoverageArea.add(block.getBuilding().getCoverageArea());
      } 
    } 
    if (pl.getPlot() != null && pl.getPlot().getArea().doubleValue() > 0.0D)
      totalCoverage = totalCoverageArea.multiply(BigDecimal.valueOf(100L)).divide(pl.getPlanInformation().getPlotArea(), 2, DcrConstants.ROUNDMODE_MEASUREMENTS); 
    pl.setCoverage(totalCoverage);
    if (pl.getVirtualBuilding() != null)
      pl.getVirtualBuilding().setTotalCoverageArea(totalCoverageArea); 
    
    BigDecimal plotArea = pl.getPlot().getArea();
    if (plotArea != null) {
      String typeOfArea = pl.getPlanInformation().getTypeOfArea();
      OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();
      if ((mostRestrictiveOccupancyType.getType() != null
				&& DxfFileConstants.A.equalsIgnoreCase(mostRestrictiveOccupancyType.getType().getCode()))
				|| (mostRestrictiveOccupancyType.getSubtype() != null
						&& (A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())
								|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())))) {
    	  String crz = pl.getPlanInfoProperties().get("CRZ_AREA");
    	  Boolean ewsPlot = isEwsPlot(pl);
  		Boolean ewsBuilding = isEwsBuilding(pl);
  		Boolean CRZZone=false;
  		String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
  		if(crzValue!=null && crzValue.equalsIgnoreCase(DcrConstants.YES))
  		{
  			CRZZone=true;
  		} 
  		if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)) {
  			
  			if (CRZZone) {
  				switch (crz) {
  				case DxfFileConstants_Pondicherry.CRZ2:
  					if (ewsPlot) {
  						expectedCoverage=EIGHTY;
  					} else if (ewsBuilding) {
  						expectedCoverage=SEVENTY_FIVE;
  					} else {
  						expectedCoverage=SEVENTY_FIVE;
  					}
  					break;
  				}
  			} else {
  				if (ewsPlot) {
  					expectedCoverage=EIGHTY;
  				} else if (ewsBuilding) {
  					expectedCoverage=SEVENTY_FIVE;
  				} else {
  					expectedCoverage=SEVENTY_FIVE;
  				}
  			}
  		}
  		else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)) {
  			
  			if (CRZZone) {
  				switch (crz) {
  				case DxfFileConstants_Pondicherry.CRZ2:
  					if (ewsPlot) {
  						expectedCoverage=FORTY;
  					} else if (ewsBuilding) {
  						expectedCoverage=FORTY;
  					} else {
  						expectedCoverage=FORTY;
  					}
  					break;
  				}
  			}
  		}
  		else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)) {
  			
  			if (CRZZone) {
  				switch (crz) {
  				case DxfFileConstants_Pondicherry.CRZ2:
  					if (ewsPlot) {
  						expectedCoverage=EIGHTY;
  					} else if (ewsBuilding) {
  						expectedCoverage=SEVENTY_FIVE;
  					} else {
  						expectedCoverage=SEVENTY_FIVE;
  					}
  					break;
  				case DxfFileConstants_Pondicherry.CRZ3:
  					if (ewsPlot) {
  						expectedCoverage=EIGHTY;
  					} else if (ewsBuilding) {
  						expectedCoverage=SEVENTY_FIVE;
  					} else {
  						expectedCoverage=SEVENTY_FIVE;
  					}
  					break;

  				}
  			} else {
  				if (ewsPlot) {
  					expectedCoverage=EIGHTY;
  				} else if (ewsBuilding) {
  					expectedCoverage=SEVENTY_FIVE;
  				} else {
  					expectedCoverage=SEVENTY_FIVE;
  				}
  			}
  		}
  		else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
  			
  			if (CRZZone) {
  				switch (crz) {
  				case DxfFileConstants_Pondicherry.CRZ2:
  					if (ewsPlot) {
  						expectedCoverage=EIGHTY;
  					} else if (ewsBuilding) {
  						expectedCoverage=SEVENTY;
  					} else {
  						expectedCoverage=SEVENTY;
  					}
  					break;
  				case DxfFileConstants_Pondicherry.CRZ3:
  					if (ewsPlot) {
  						expectedCoverage=EIGHTY;
  					} else if (ewsBuilding) {
  						expectedCoverage=SEVENTY;
  					} else {
  						expectedCoverage=SEVENTY;
  					}
  					break;

  				}
  			} else {
  				if (ewsPlot) {
  					expectedCoverage=EIGHTY;
  				} else if (ewsBuilding) {
  					expectedCoverage=SEVENTY;
  				} else {
  					expectedCoverage=SEVENTY;
  				}
  			}
  		}
  		processCoverageResidential(pl, "", totalCoverage, expectedCoverage);
		}
    } 
    return pl;
  }
  
  private void processCoverageResidential(Plan pl, String occupancy, BigDecimal coverage, BigDecimal upperLimit) {
	    ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
	    scrutinyDetail.setKey("Common_Coverage");
	    scrutinyDetail.setHeading("Coverage in Percentage");
	    scrutinyDetail.addColumnHeading(Integer.valueOf(1), "Byelaw");
	    scrutinyDetail.addColumnHeading(Integer.valueOf(2), "Description");
	    scrutinyDetail.addColumnHeading(Integer.valueOf(4), "Permissible");
	    scrutinyDetail.addColumnHeading(Integer.valueOf(5), "Provided");
	    scrutinyDetail.addColumnHeading(Integer.valueOf(6), "Status");
	    String desc = getLocaleMessage(RULE_DESCRIPTION_KEY, new String[] { upperLimit.toString() });
	    String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, new String[] { coverage.toString() });
	    String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, new String[] { upperLimit.toString() });
	    if (coverage.doubleValue() <= upperLimit.doubleValue()) {
	      Map<String, String> details = new HashMap<>();
	      details.put("Byelaw", "Part-II Table-1");
	      details.put("Description", desc);
	      details.put("Permissible", expectedResult);
	      details.put("Provided", actualResult);
	      details.put("Status", Result.Accepted.getResultVal());
	      scrutinyDetail.getDetail().add(details);
	      pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	    } else {
	      Map<String, String> details = new HashMap<>();
	      details.put("Byelaw", "Part-II Table-1");
	      details.put("Description", desc);
	      details.put("Permissible", expectedResult);
	      details.put("Provided", actualResult);
	      details.put("Status", Result.Not_Accepted.getResultVal());
	      scrutinyDetail.getDetail().add(details);
	      pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	    } 
	  }
  
  private void processCoverage(Plan pl, String occupancy, BigDecimal coverage, BigDecimal upperLimit) {
    ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
    scrutinyDetail.setKey("Common_Coverage");
    scrutinyDetail.setHeading("Coverage in Percentage");
    scrutinyDetail.addColumnHeading(Integer.valueOf(1), "Byelaw");
    scrutinyDetail.addColumnHeading(Integer.valueOf(2), "Description");
    scrutinyDetail.addColumnHeading(Integer.valueOf(4), "Permissible");
    scrutinyDetail.addColumnHeading(Integer.valueOf(5), "Provided");
    scrutinyDetail.addColumnHeading(Integer.valueOf(6), "Status");
    String desc = getLocaleMessage(RULE_DESCRIPTION_KEY, new String[] { upperLimit.toString() });
    String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, new String[] { coverage.toString() });
    String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, new String[] { upperLimit.toString() });
    if (coverage.doubleValue() <= upperLimit.doubleValue()) {
      Map<String, String> details = new HashMap<>();
      details.put("Byelaw", "38");
      details.put("Description", desc);
      details.put("Permissible", expectedResult);
      details.put("Provided", actualResult);
      details.put("Status", Result.Accepted.getResultVal());
      scrutinyDetail.getDetail().add(details);
      pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    } else {
      Map<String, String> details = new HashMap<>();
      details.put("Byelaw", "38");
      details.put("Description", desc);
      details.put("Permissible", expectedResult);
      details.put("Provided", actualResult);
      details.put("Status", Result.Not_Accepted.getResultVal());
      scrutinyDetail.getDetail().add(details);
      pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    } 
  }
  
  protected OccupancyType getMostRestrictiveCoverage(EnumSet<OccupancyType> distinctOccupancyTypes) {
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B1))
      return OccupancyType.OCCUPANCY_B1; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B2))
      return OccupancyType.OCCUPANCY_B2; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B3))
      return OccupancyType.OCCUPANCY_B3; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D))
      return OccupancyType.OCCUPANCY_D; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D1))
      return OccupancyType.OCCUPANCY_D1; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I2))
      return OccupancyType.OCCUPANCY_I2; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I1))
      return OccupancyType.OCCUPANCY_I1; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_C))
      return OccupancyType.OCCUPANCY_C; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A1))
      return OccupancyType.OCCUPANCY_A1; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A4))
      return OccupancyType.OCCUPANCY_A4; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A2))
      return OccupancyType.OCCUPANCY_A2; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G1))
      return OccupancyType.OCCUPANCY_G1; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_E))
      return OccupancyType.OCCUPANCY_E; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F))
      return OccupancyType.OCCUPANCY_F; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F4))
      return OccupancyType.OCCUPANCY_F4; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G2))
      return OccupancyType.OCCUPANCY_G2; 
    if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_H))
      return OccupancyType.OCCUPANCY_H; 
    return null;
  }
  
	private Boolean isEwsBuilding(Plan pl) {
		if(pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.EWS_BUILDING).equalsIgnoreCase(DcrConstants.YES))
			return true;
		return false;
	}

	private Boolean isEwsPlot(Plan pl) {
		if(pl.getPlanInformation().getPlotArea().compareTo(BigDecimal.valueOf(100l)) < 0)
			return true;
		else return false;
	}
  
  public Map<String, Date> getAmendments() {
    return new LinkedHashMap<>();
  }
}
