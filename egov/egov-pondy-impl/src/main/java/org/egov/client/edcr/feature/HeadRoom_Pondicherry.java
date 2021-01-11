package org.egov.client.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_R;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.client.edcr.constants.DxfFileConstants_Pondicherry;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.HeadRoom;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class HeadRoom_Pondicherry extends HeadRoom {
  private static final BigDecimal TWO_POINTTWO = BigDecimal.valueOf(2.2D);
  
  private static final BigDecimal TWO_POINTONE = BigDecimal.valueOf(2.1D);
    
  public Plan validate(Plan plan) {
    return plan;
  }
  
  public Plan process(Plan plan) {
    for (Block block : plan.getBlocks()) {
      if (block.getBuilding() != null) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(Integer.valueOf(1), "Byelaw");
        scrutinyDetail.addColumnHeading(Integer.valueOf(2), "Description");
        scrutinyDetail.addColumnHeading(Integer.valueOf(3), "Required");
        scrutinyDetail.addColumnHeading(Integer.valueOf(4), "Provided");
        scrutinyDetail.addColumnHeading(Integer.valueOf(5), "Status");
        scrutinyDetail.setKey("Block_" + block.getNumber() + "_Stair Headroom");
        org.egov.common.entity.edcr.HeadRoom headRoom = block.getBuilding().getHeadRoom();
        if (headRoom != null) {
          List<BigDecimal> headRoomDimensions = headRoom.getHeadRoomDimensions();
          if (headRoomDimensions != null && headRoomDimensions.size() > 0) {
            BigDecimal minHeadRoomDimension = headRoomDimensions.stream().reduce(BigDecimal::min).get();
            BigDecimal minWidth = Util.roundOffTwoDecimal(minHeadRoomDimension);
            HashMap<String, String> errorMsgs = new HashMap<>();
            String typeOfArea = plan.getPlanInformation().getTypeOfArea();
            OccupancyTypeHelper mostRestrictiveOccupancyType = plan.getVirtualBuilding().getMostRestrictiveFarHelper();
            if ((mostRestrictiveOccupancyType.getType() != null
    				&& DxfFileConstants.A.equalsIgnoreCase(mostRestrictiveOccupancyType.getType().getCode()))
    				|| (mostRestrictiveOccupancyType.getSubtype() != null
    						&& (A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())
    								|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())))) {
    			processPlotareaResidential(plan, mostRestrictiveOccupancyType, minWidth, typeOfArea,scrutinyDetail, errorMsgs);
    		}      
            
            

          } 
        } 
      } 
    } 
    return plan;
  }
  
  
	//pondy customization

	private void processPlotareaResidential(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal minWidth, String typeOfArea,ScrutinyDetail scrutinyDetail,HashMap<String, String> errors) {

		String expectedResult = StringUtils.EMPTY;
		boolean isAccepted = false;
	// getting additoinal property crz_area
		String crz = pl.getPlanInfoProperties().get("CRZ_AREA");
		// check it is ews
		Boolean ewsPlot = isEwsPlot(pl);
		Boolean ewsBuilding = isEwsBuilding(pl);
		Boolean CRZZone=false;
		if(ewsPlot) {
		}
		if(ewsBuilding) {
		}
		String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
	
		if(crzValue!=null && crzValue.equalsIgnoreCase(DcrConstants.YES))
		{
			CRZZone=true;
		} 
		if (CRZZone) {
			}
		
if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)) {
			
			if (CRZZone) {
				switch (crz) {
				case DxfFileConstants_Pondicherry.CRZ2:
					if (ewsPlot) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else if (ewsBuilding) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else {
						isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
						expectedResult = ">=" + TWO_POINTTWO;
					}
					break;
				}
			} else {
				if (ewsPlot) {
					isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
					expectedResult = ">=" + TWO_POINTONE;
				} else if (ewsBuilding) {
					isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
					expectedResult = ">=" + TWO_POINTONE;
				} else {
					isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
					expectedResult = ">=" + TWO_POINTTWO;
				}
			}
		}
		else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)) {
			
			if (CRZZone) {
				switch (crz) {
				case DxfFileConstants_Pondicherry.CRZ2:
					if (ewsPlot) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else if (ewsBuilding) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else {
						isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
						expectedResult = ">=" + TWO_POINTTWO;
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
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else if (ewsBuilding) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else {
						isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
						expectedResult = ">=" + TWO_POINTTWO;
					}
					break;
				case DxfFileConstants_Pondicherry.CRZ3:
					if (ewsPlot) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else if (ewsBuilding) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else {
						isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
						expectedResult = ">=" + TWO_POINTTWO;
					}
					break;

				}
			} else {
				if (ewsPlot) {
					isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
					expectedResult = ">=" + TWO_POINTONE;
				} else if (ewsBuilding) {
					isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
					expectedResult = ">=" + TWO_POINTONE;
				} else {
					isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
					expectedResult = ">=" + TWO_POINTTWO;
				}
			}
		}
		else if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
			
			if (CRZZone) {
				switch (crz) {
				case DxfFileConstants_Pondicherry.CRZ2:
					if (ewsPlot) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else if (ewsBuilding) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else {
						isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
						expectedResult = ">=" + TWO_POINTTWO;
					}
					break;
				case DxfFileConstants_Pondicherry.CRZ3:
					if (ewsPlot) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else if (ewsBuilding) {
						isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
						expectedResult = ">=" + TWO_POINTONE;
					} else {
						isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
						expectedResult = ">=" + TWO_POINTTWO;
					}
					break;

				}
			} else {
				if (ewsPlot) {
					isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
					expectedResult = ">=" + TWO_POINTONE;
				} else if (ewsBuilding) {
					isAccepted = minWidth.compareTo(TWO_POINTONE) >= 0;
					expectedResult = ">=" + TWO_POINTONE;
				} else {
					isAccepted = minWidth.compareTo(TWO_POINTTWO) >= 0;
					expectedResult = ">=" + TWO_POINTTWO;
				}
			}
		}
	
		if (StringUtils.isNotBlank(expectedResult)) {
			buildResult(pl, expectedResult, minWidth,scrutinyDetail , isAccepted);
		}

	}

      
	private void buildResult(Plan pl, String expected, BigDecimal actual, ScrutinyDetail scrutinyDetail, boolean isAccepted) {
	    Map<String, String> details = new HashMap<>();
	    details.put("Byelaw", "Part-I Clause 30 (xi), 38(5)(a)");
	    details.put("Description", "Minimum clear of stair head-room");
	    details.put("Required", expected);
	    details.put("Provided", actual.toString());
	    details.put("Status", isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal()); 
	    scrutinyDetail.getDetail().add(details);
	    pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
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
	
  public Map<String, Date> getAmendments() {
    return new LinkedHashMap<>();
  }
}
