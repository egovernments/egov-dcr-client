package org.egov.client.edcr.feature;

import static org.egov.edcr.utility.DcrConstants.BUILDING_HEIGHT;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.client.edcr.constants.DxfFileConstants_Pondicherry;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.FeatureProcess;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BuildingHeight_Pondicherry extends FeatureProcess
{
  private static final Logger LOG = Logger.getLogger(BuildingHeight_Pondicherry.class);
  private static final String RULE_EXPECTED_KEY = "buildingheight.expected";
  private static final String RULE_ACTUAL_KEY = "buildingheight.actual";
  private static final String RULE = "Height of Building";
  private static final String SUB_RULE = "Part-II Table-1 Amended 09/04/2020";
  public static final String DECLARED = "Declared";
  private static final BigDecimal NINE = BigDecimal.valueOf(9L);
  private static final BigDecimal TEN = BigDecimal.valueOf(10L);
  private static final BigDecimal TENPOINTFIVE = BigDecimal.valueOf(10.5D);
  private static final BigDecimal TWELVE = BigDecimal.valueOf(12L);
  
  public Plan validate(Plan pl) {
	  HashMap<String, String> errors = new HashMap<>();
          for (Block block : pl.getBlocks()) {
              if (!block.getCompletelyExisting()) {
                  if (block.getBuilding() != null && (block.getBuilding().getBuildingHeight() == null ||
                          block.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) <= 0)) {
                      errors.put(BUILDING_HEIGHT + block.getNumber(),
                              prepareMessage(OBJECTNOTDEFINED, BUILDING_HEIGHT + " for block " + block.getNumber()));
                      pl.addErrors(errors);
                  }
              }
          }
	  return pl;
  }

  public Plan process(Plan pl) {
	  validate(pl);
	  scrutinyDetail = new ScrutinyDetail();
      scrutinyDetail.setKey("Common_Height of Building");
      scrutinyDetail.addColumnHeading(1, RULE_NO);
      scrutinyDetail.addColumnHeading(2, DESCRIPTION);
      scrutinyDetail.addColumnHeading(3, PERMISSIBLE);
      scrutinyDetail.addColumnHeading(4, PROVIDED);
      scrutinyDetail.addColumnHeading(5, STATUS);
      checkBuildingHeight(pl);
	  return pl;
  }

  private void checkBuildingHeight(Plan pl) {
    String subRule = "";
    for (Block block : pl.getBlocks()) {
    	if (!block.getCompletelyExisting()) {
    		BigDecimal exptectedDistance = BigDecimal.ZERO;
    		BigDecimal actualDistance = BigDecimal.ZERO;
    	    actualDistance = block.getBuilding().getBuildingHeight();
    	    
    	    String typeOfArea = pl.getPlanInformation().getTypeOfArea();
    		String crz = pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.CRZ_AREA);
    		String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
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
    								exptectedDistance = TENPOINTFIVE;
    								subRule = SUB_RULE;
    							}
    							else
    							{
    								pl.addError("Invalid", "Regular Building not allowed in EWS plot (Building Height)");
    							}
    						}
    						else
    						{
    							if(ewsBuilding) {
    								exptectedDistance = TENPOINTFIVE;
    								subRule = SUB_RULE;
    							}
    							else
    							{
    								exptectedDistance = TENPOINTFIVE;
    								subRule = SUB_RULE;
    							}
    						}
    					}
    					else
    					{
    						pl.addError("Invalid", "Invalid classification of area type is defined (Building Height)");
    					}
    					break;
    				case DxfFileConstants_Pondicherry.CRZ3:
    					if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)
    							|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
	    					if (ewsPlot) {
	    						if(ewsBuilding) {
		    						exptectedDistance = NINE;
		    						subRule = SUB_RULE;
	    						}
	    						else
	    						{
	    							pl.addError("Invalid", "Regular Building not allowed in EWS plot (Building Height)");
	    						}
	    					}
	    					else
	    					{
	    						if(ewsBuilding) {
		    						exptectedDistance = NINE;
		    						subRule = SUB_RULE;
	    						}
	    						else
	    						{
	    							exptectedDistance = NINE;
		    						subRule = SUB_RULE;
	    						}
	    					}
    					}
    					else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)
    							|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)) {
	    					if (ewsPlot) {
	    						if(ewsBuilding) {
		    						exptectedDistance = BigDecimal.ZERO;
		    						subRule = DcrConstants.NA;
	    						}
	    						else
	    						{
	    							pl.addError("Invalid", "Regular Building not allowed in EWS plot (Building Height)");
	    						}
	    					}
	    					else
	    					{
	    						if(ewsBuilding) {
	    							exptectedDistance = BigDecimal.ZERO;
		    						subRule = DcrConstants.NA;
	    						}
	    						else
	    						{
	    							exptectedDistance = BigDecimal.ZERO;
		    						subRule = DcrConstants.NA;
	    						}
	    					}
    					}
    					else
    					{
    						pl.addError("Invalid", "Invalid classification of area type is defined (Building Height)");
    					}
    					break;
    				default:
    					pl.addError("Invalid", "Invalid CRZ is defined (Building Height)");
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
							exptectedDistance = TENPOINTFIVE;
							subRule = SUB_RULE;
						}
						else
						{
							pl.addError("Invalid", "Regular Building not allowed in EWS plot (Building Height)");
						}
					}
					else
					{
						if(ewsBuilding) {
							exptectedDistance = TENPOINTFIVE;
							subRule = SUB_RULE;
						}
						else
						{
							exptectedDistance = TENPOINTFIVE;
							subRule = SUB_RULE;
						}
					}
				}
    			else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)) {
					if (ewsPlot) {
						if(ewsBuilding) {
    						exptectedDistance = BigDecimal.ZERO;
    						subRule = DcrConstants.NA;
						}
						else
						{
							pl.addError("Invalid", "Regular Building not allowed in EWS plot (Building Height)");
						}
					}
					else
					{
						if(ewsBuilding) {
							exptectedDistance = BigDecimal.ZERO;
    						subRule = DcrConstants.NA;
						}
						else
						{
							exptectedDistance = BigDecimal.ZERO;
    						subRule = DcrConstants.NA;
						}
					}
				}
				else
				{
					pl.addError("Invalid", "Invalid classification of area type is defined (Building Height)");
				}
    		}
    	      
    	    if (exptectedDistance.compareTo(BigDecimal.ZERO) > 0) {
    	        String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, new String[] { actualDistance.toString() });
    	        String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, new String[] { exptectedDistance.toString() });
    	        
    	        if (actualDistance.compareTo(exptectedDistance) > 0) {
    	        	Map<String, String> details = new HashMap<String, String>();
    	        	details.put(RULE_NO, subRule);
    	        	details.put(DESCRIPTION, RULE + " for Block " + block.getNumber());
    	        	details.put(PERMISSIBLE, expectedResult);
    	        	details.put(PROVIDED, actualResult);
    	        	details.put(STATUS, Result.Not_Accepted.getResultVal());
    	        	scrutinyDetail.getDetail().add(details);
    	        	pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    	        	continue;
    	        } 
    	        else
    	        {
    		        Map<String, String> details = new HashMap<String, String>();
    		        details.put(RULE_NO, subRule);
    		        details.put(DESCRIPTION, RULE + " for Block " + block.getNumber());
    		        details.put(PERMISSIBLE, expectedResult);
    		        details.put(PROVIDED, actualResult);
    		        details.put(STATUS, Result.Verify.getResultVal());
    		        scrutinyDetail.getDetail().add(details);
    		        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    	        }
    	    }
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
  
  public String prepareMessage(String code, String... args) {
      return edcrMessageSource.getMessage(code, args, LocaleContextHolder.getLocale());
  }
  
  public Map<String, Date> getAmendments() {
	  return new LinkedHashMap();
  }
}
