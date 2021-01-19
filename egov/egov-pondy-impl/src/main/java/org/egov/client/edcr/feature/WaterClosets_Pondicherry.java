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
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.RoomHeight;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.WaterClosets;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.stereotype.Service;

@Service
public class WaterClosets_Pondicherry extends WaterClosets {
  private static final Logger LOG = Logger.getLogger(WaterClosets_Pondicherry.class);
  
  private static final String RULE_41_IV = "41-iv";
  
  public static final String WATERCLOSETS_DESCRIPTION = "Water Closets";
  
  public static final BigDecimal ONE = BigDecimal.valueOf(1L);
  
  public static final BigDecimal ONE_ONE_FIVE = BigDecimal.valueOf(1.15D);
  
  public static final BigDecimal TWO_FIVE = BigDecimal.valueOf(2.5D);
  
  public static final BigDecimal TWO_ONE = BigDecimal.valueOf(2.1D);
  
  public static final BigDecimal NINE = BigDecimal.valueOf(0.9D);
  
  
  public Plan validate(Plan pl) {
    return pl;
  }
  
  public Plan process(Plan pl) {
	  LOG.info("Inside Water closets processing");
    ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
    scrutinyDetail.setKey("Common_Water Closets");
    scrutinyDetail.addColumnHeading(Integer.valueOf(1), "Byelaw");
    scrutinyDetail.addColumnHeading(Integer.valueOf(2), "Description");
    scrutinyDetail.addColumnHeading(Integer.valueOf(3), "Required");
    scrutinyDetail.addColumnHeading(Integer.valueOf(4), "Provided");
    scrutinyDetail.addColumnHeading(Integer.valueOf(5), "Status");
    Map<String, String> details = new HashMap<>();
    details.put("Byelaw", "Part-I Clause 32 (3)(a)");
    details.put("Description", WATERCLOSETS_DESCRIPTION);
    BigDecimal minHeight = BigDecimal.ZERO, totalArea = BigDecimal.ZERO, minWidth = BigDecimal.ZERO;
    BigDecimal expectedHeight = BigDecimal.ZERO, expectedArea = BigDecimal.ZERO, expectedWidth = BigDecimal.ZERO;
    String typeOfArea = pl.getPlanInformation().getTypeOfArea();
    for (Block b : pl.getBlocks()) {
      if (b.getBuilding() != null && b.getBuilding().getFloors() != null && 
        !b.getBuilding().getFloors().isEmpty())
        for (Floor f : b.getBuilding().getFloors()) {
          if (f.getWaterClosets() != null && f.getWaterClosets().getHeights() != null && 
            !f.getWaterClosets().getHeights().isEmpty() && f.getWaterClosets().getRooms() != null && 
            !f.getWaterClosets().getRooms().isEmpty()) {
            if (f.getWaterClosets().getHeights() != null && !f.getWaterClosets().getHeights().isEmpty()) {
              minHeight = ((RoomHeight)f.getWaterClosets().getHeights().get(0)).getHeight();
              for (RoomHeight rh : f.getWaterClosets().getHeights()) {
                if (rh.getHeight().compareTo(minHeight) < 0)
                  minHeight = rh.getHeight(); 
              } 
            } 
            if (f.getWaterClosets().getRooms() != null && !f.getWaterClosets().getRooms().isEmpty()) {
              minWidth = ((Measurement)f.getWaterClosets().getRooms().get(0)).getWidth();
              for (Measurement m : f.getWaterClosets().getRooms()) {
                totalArea = totalArea.add(m.getArea());
                if (m.getWidth().compareTo(minWidth) < 0)
                  minWidth = m.getWidth(); 
              } 
            } 
            
            

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
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else if (ewsBuilding) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else {
    						expectedHeight = ONE;
    						expectedArea = ONE_ONE_FIVE;
    						expectedWidth = TWO_FIVE;
    					}
    					break;
    				}
    			} else {
					if (ewsPlot) {
						expectedHeight = NINE;
						expectedArea = NINE;
						expectedWidth = TWO_ONE;
					} else if (ewsBuilding) {
						expectedHeight = NINE;
						expectedArea = NINE;
						expectedWidth = TWO_ONE;
					} else {
						expectedHeight = ONE;
						expectedArea = ONE_ONE_FIVE;
						expectedWidth = TWO_FIVE;
					}
    			}
    		}
    		else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)) {
    			
    			if (CRZZone) {
    				switch (crz) {
    				case DxfFileConstants_Pondicherry.CRZ2:
    					if (ewsPlot) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else if (ewsBuilding) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else {
    						expectedHeight = ONE;
    						expectedArea = ONE_ONE_FIVE;
    						expectedWidth = TWO_FIVE;
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
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else if (ewsBuilding) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else {
    						expectedHeight = ONE;
    						expectedArea = ONE_ONE_FIVE;
    						expectedWidth = TWO_FIVE;
    					}
    					break;
    				case DxfFileConstants_Pondicherry.CRZ3:
    					if (ewsPlot) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else if (ewsBuilding) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else {
    						expectedHeight = ONE;
    						expectedArea = ONE_ONE_FIVE;
    						expectedWidth = TWO_FIVE;
    					}
    					break;

    				}
    			} else {
					if (ewsPlot) {
						expectedHeight = NINE;
						expectedArea = NINE;
						expectedWidth = TWO_ONE;
					} else if (ewsBuilding) {
						expectedHeight = NINE;
						expectedArea = NINE;
						expectedWidth = TWO_ONE;
					} else {
						expectedHeight = ONE;
						expectedArea = ONE_ONE_FIVE;
						expectedWidth = TWO_FIVE;
					}
    			}
    		}
    		else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
    			
    			if (CRZZone) {
    				switch (crz) {
    				case DxfFileConstants_Pondicherry.CRZ2:
    					if (ewsPlot) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else if (ewsBuilding) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else {
    						expectedHeight = ONE;
    						expectedArea = ONE_ONE_FIVE;
    						expectedWidth = TWO_FIVE;
    					}
    					break;
    				case DxfFileConstants_Pondicherry.CRZ3:
    					if (ewsPlot) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else if (ewsBuilding) {
    						expectedHeight = NINE;
    						expectedArea = NINE;
    						expectedWidth = TWO_ONE;
    					} else {
    						expectedHeight = ONE;
    						expectedArea = ONE_ONE_FIVE;
    						expectedWidth = TWO_FIVE;
    					}
    					break;

    				}
    			} else {
					if (ewsPlot) {
						expectedHeight = NINE;
						expectedArea = NINE;
						expectedWidth = TWO_ONE;
					} else if (ewsBuilding) {
						expectedHeight = NINE;
						expectedArea = NINE;
						expectedWidth = TWO_ONE;
					} else {
						expectedHeight = ONE;
						expectedArea = ONE_ONE_FIVE;
						expectedWidth = TWO_FIVE;
					}
    			}
    		}
  		
            
            if (minHeight.compareTo(expectedHeight) >= 0 && totalArea
              .compareTo(expectedArea) >= 0 && minWidth
              .compareTo(expectedWidth) >= 0) {
              details.put("Required", "Height >= "+expectedHeight+", Total Area >= "+expectedArea+", Width >= "+expectedWidth);
              details.put("Provided", "Height >= " + minHeight + ", Total Area >= " + totalArea + ", Width >= " + minWidth);
              details.put("Status", Result.Accepted.getResultVal());
              scrutinyDetail.getDetail().add(details);
              pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
              continue;
            } 
            details.put("Required", "Height >= "+expectedHeight+", Total Area >= "+expectedArea+", Width >= "+expectedWidth);
            details.put("Provided", "Height >= " + minHeight + ", Total Area >= " + totalArea + ", Width >= " + minWidth);
            details.put("Status", Result.Not_Accepted.getResultVal());
            
            
            scrutinyDetail.getDetail().add(details);
            pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
          } 
        }  
    } 
    return pl;
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
