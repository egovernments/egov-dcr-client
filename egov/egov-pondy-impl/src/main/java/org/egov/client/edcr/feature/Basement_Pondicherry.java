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
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.FeatureProcess;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class Basement_Pondicherry extends FeatureProcess
{
	private static final Logger LOG = Logger.getLogger(Basement_Pondicherry.class);
	private static final String SUB_RULE = "Part-I Clause 30 (IX)";
	private static final String RULE = "Basement Floor";
	private static final BigDecimal TWO_POINT_TWO = BigDecimal.valueOf(2.2D);
	public static final String BASEMENT_DESCRIPTION_ONE = "Height from the floor to the soffit of the roof slab or ceiling";
	public static final String BASEMENT_DESCRIPTION_TWO = "Minimum height of the ceiling of upper basement above ground level";
  
	public Plan validate(Plan pl) { 
		return pl; 
	}
  
	public Plan process(Plan pl) {
		validate(pl);	    
    
		if (pl.getBlocks() != null) {
			for (Block block : pl.getBlocks()) {
				ScrutinyDetail scrutinyDetail = getNewScrutinyDetail("Block_" + block.getNumber() + "_Basement");
			    BigDecimal floorHeight = BigDecimal.ZERO;
			    String typeOfArea = pl.getPlanInformation().getTypeOfArea();
			    String crz = pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.CRZ_AREA);
				String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
				Boolean ewsBuilding = isEwsBuilding(pl);
				Boolean ewsPlot = isEwsPlot(pl);
				Boolean stiltFloor = isStiltFloor(pl);
				Boolean CRZZone = false;
				
				if (crzValue != null && crzValue.equalsIgnoreCase(DcrConstants.YES)) {
					CRZZone = true;
				}
				
				if (block.getBuilding() != null && block.getBuilding().getFloors() != null && !block.getBuilding().getFloors().isEmpty())
				{
					for (Floor floor : block.getBuilding().getFloors()) {
						if (floor.getNumber().intValue() == -1 && !stiltFloor) {
							if (floor.getHeightFromTheFloorToCeiling() != null && !floor.getHeightFromTheFloorToCeiling().isEmpty()) {
								floorHeight = (BigDecimal)floor.getHeightFromTheFloorToCeiling().stream().reduce(BigDecimal::min).get();
								if (CRZZone) {
									switch (crz) {
										case DxfFileConstants_Pondicherry.CRZ2:
											if (typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.WHITE_TOWN)
													|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.TAMIL_TOWN)
														|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OUTSIDE_BOULEVARD)
															|| typeOfArea.equalsIgnoreCase(DxfFileConstants_Pondicherry.OTHER_AREA)) {
												if (ewsPlot) {
					  								if(ewsBuilding) {
					  									if(floorHeight.compareTo(TWO_POINT_TWO) >= 0)
					  									{
					  										setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Accepted.getResultVal());
					  									}
					  									else
					  									{
					  										setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Not_Accepted.getResultVal());
					  									}
					  								}
					  								else
					  								{
					  									pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
					  								}
					  							}
					  							else
					  							{
					  								if(floorHeight.compareTo(TWO_POINT_TWO) >= 0)
				  									{
				  										setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Accepted.getResultVal());
				  									}
				  									else
				  									{
				  										setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Not_Accepted.getResultVal());
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
					  									if(floorHeight.compareTo(TWO_POINT_TWO) >= 0)
					  									{
					  										setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Accepted.getResultVal());
					  									}
					  									else
					  									{
					  										setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Not_Accepted.getResultVal());
					  									}
					  								}
					  								else
					  								{
					  									pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
					  								}
					  							}
					  							else
					  							{
					  								if(floorHeight.compareTo(TWO_POINT_TWO) >= 0)
					  								{
					  									setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Accepted.getResultVal());
					  								}
					  								else
					  								{
					  									setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Not_Accepted.getResultVal());
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
					  							if(floorHeight.compareTo(TWO_POINT_TWO) >= 0)
					  							{
					  								setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Accepted.getResultVal());
					  							}
					  							else
					  							{
					  								setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Not_Accepted.getResultVal());
					  							}
					  						}
					  						else
					  						{
					  							pl.addError("Invalid", "Regular Building is not allowed in EWS plot");
					  						}
					  					}
					  					else
					  					{
					  						if(floorHeight.compareTo(TWO_POINT_TWO) >= 0)
					  						{
					  							setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Accepted.getResultVal());
					  						}
					  						else
					  						{
					  							setReportOutputDetails(pl, SUB_RULE, RULE, TWO_POINT_TWO +"(MTR)", floorHeight, Result.Not_Accepted.getResultVal());
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
							pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
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
	
	public Boolean isStiltFloor(Plan pl) {
		if(StringUtils.isNotBlank(pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.STILT_FLOOR)) 
				&& pl.getPlanInfoProperties().get(DxfFileConstants_Pondicherry.STILT_FLOOR).equalsIgnoreCase(DcrConstants.YES)) 
			return true;
		else 
			return false;
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
  
	private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String expected, BigDecimal actual, String status) {
		Map<String, String> details = new HashMap<String, String>();
	    details.put(RULE_NO, ruleNo);
	    details.put(DESCRIPTION, ruleDesc);
	    details.put(PERMISSIBLE, expected);
	    details.put(PROVIDED, actual.toString());
	    details.put(STATUS, status);
	    this.scrutinyDetail.getDetail().add(details);
	    pl.getReportOutput().getScrutinyDetails().add(this.scrutinyDetail);
	}
  
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap();
	}
}
