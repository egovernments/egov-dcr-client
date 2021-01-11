/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2019>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.client.edcr;

import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.A2;
import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_FH;
import static org.egov.edcr.constants.DxfFileConstants.A_R;
import static org.egov.edcr.constants.DxfFileConstants.A_SA;
import static org.egov.edcr.constants.DxfFileConstants.D_A;
import static org.egov.edcr.constants.DxfFileConstants.D_B;
import static org.egov.edcr.constants.DxfFileConstants.D_C;
import static org.egov.edcr.constants.DxfFileConstants.E_CLG;
import static org.egov.edcr.constants.DxfFileConstants.E_EARC;
import static org.egov.edcr.constants.DxfFileConstants.E_NS;
import static org.egov.edcr.constants.DxfFileConstants.E_PS;
import static org.egov.edcr.constants.DxfFileConstants.E_SACA;
import static org.egov.edcr.constants.DxfFileConstants.E_SFDAP;
import static org.egov.edcr.constants.DxfFileConstants.E_SFMC;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.H_PP;
import static org.egov.edcr.constants.DxfFileConstants.M_DFPAB;
import static org.egov.edcr.constants.DxfFileConstants.M_HOTHC;
import static org.egov.edcr.constants.DxfFileConstants.M_NAPI;
import static org.egov.edcr.constants.DxfFileConstants.M_OHF;
import static org.egov.edcr.constants.DxfFileConstants.M_VH;
import static org.egov.edcr.constants.DxfFileConstants.S_BH;
import static org.egov.edcr.constants.DxfFileConstants.S_CA;
import static org.egov.edcr.constants.DxfFileConstants.S_CRC;
import static org.egov.edcr.constants.DxfFileConstants.S_ECFG;
import static org.egov.edcr.constants.DxfFileConstants.S_ICC;
import static org.egov.edcr.constants.DxfFileConstants.S_MCH;
import static org.egov.edcr.constants.DxfFileConstants.S_SAS;
import static org.egov.edcr.constants.DxfFileConstants.S_SC;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.PLOT_AREA;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Building;
import org.egov.common.entity.edcr.FarDetails;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.feature.Far;
import org.egov.edcr.service.ProcessPrintHelper;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class Far_pondicherry extends Far {

	private static final String BUILDING_TYPE = "Building Type";
	private static final String PLOT_TYPE = "Plot Type";

	private static final Logger LOG = Logger.getLogger(Far_pondicherry.class);

	private static final String VALIDATION_NEGATIVE_FLOOR_AREA = "msg.error.negative.floorarea.occupancy.floor";
	private static final String VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA = "msg.error.negative.existing.floorarea.occupancy.floor";
	private static final String VALIDATION_NEGATIVE_BUILTUP_AREA = "msg.error.negative.builtuparea.occupancy.floor";
	private static final String VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA = "msg.error.negative.existing.builtuparea.occupancy.floor";
	public static final String RULE_38 = "CDP-2036";
	public static final String RULE_CRZ_II = "CRZ-II/ INTACH Norms";

	private static final BigDecimal ONE_POINTTWO = BigDecimal.valueOf(1.2);
	private static final BigDecimal ONE_POINTFIVE = BigDecimal.valueOf(1.5);
	private static final BigDecimal TWO_TWO = BigDecimal.valueOf(2.2);
	private static final BigDecimal THREE = BigDecimal.valueOf(3);

	public static final String TAMIL_TOWN = "TAMIL TOWN";
	public static final String WHITE_TOWN = "WHITE TOWN";
	public static final String OTHER_AREA = "OTHER AREA";
	public static final String OUTSIDE_BOULEVARD = "OUTSIDE BOULEVARD";
	public static final String CRZ1 = "CRZ-I";
	public static final String CRZ2 = "CRZ-II";
	public static final String CRZ3 = "CRZ-III";
	private static final String CRZ_AREA = "CRZ Area";
	private static final String REGULAR = "Regular";

	public static final String OLD_AREA_ERROR = "road width old area";
	public static final String NEW_AREA_ERROR = "road width new area";
	public static final String OLD_AREA_ERROR_MSG = "No construction shall be permitted if the road width is less than 2.4m for old area.";
	public static final String NEW_AREA_ERROR_MSG = "No construction shall be permitted if the road width is less than 6.1m for new area.";

	

	@Override
	public Plan validate(Plan pl) {
		if (pl.getPlot() == null || (pl.getPlot() != null
				&& (pl.getPlot().getArea() == null || pl.getPlot().getArea().doubleValue() == 0))) {
			pl.addError(PLOT_AREA, getLocaleMessage(OBJECTNOTDEFINED, PLOT_AREA));
		}
		return pl;
	}

	@Override
	public Plan process(Plan pl) {
		try{
		decideNocIsRequired(pl);
		HashMap<String, String> errorMsgs = new HashMap<>();
		int errors = pl.getErrors().size();
		validate(pl);
		int validatedErrors = pl.getErrors().size();
		if (validatedErrors > errors) {
			return pl;
		}

		// calculate
		BigDecimal totalExistingBuiltUpArea = BigDecimal.ZERO;
		BigDecimal totalExistingFloorArea = BigDecimal.ZERO;
		BigDecimal totalBuiltUpArea = BigDecimal.ZERO;
		BigDecimal totalFloorArea = BigDecimal.ZERO;
		BigDecimal totalCarpetArea = BigDecimal.ZERO;
		BigDecimal totalExistingCarpetArea = BigDecimal.ZERO;
		Set<OccupancyTypeHelper> distinctOccupancyTypesHelper = new HashSet<>();
		for (Block blk : pl.getBlocks()) {
			BigDecimal flrArea = BigDecimal.ZERO;
			BigDecimal bltUpArea = BigDecimal.ZERO;
			BigDecimal existingFlrArea = BigDecimal.ZERO;
			BigDecimal existingBltUpArea = BigDecimal.ZERO;
			BigDecimal carpetArea = BigDecimal.ZERO;
			BigDecimal existingCarpetArea = BigDecimal.ZERO;
			Building building = blk.getBuilding();
			for (Floor flr : building.getFloors()) {
				for (Occupancy occupancy : flr.getOccupancies()) {
					validate2(pl, blk, flr, occupancy);
					/*
					 * occupancy.setCarpetArea(occupancy.getFloorArea().multiply
					 * (BigDecimal.valueOf(0.80))); occupancy
					 * .setExistingCarpetArea(occupancy.getExistingFloorArea().
					 * multiply(BigDecimal.valueOf(0.80)));
					 */

					bltUpArea = bltUpArea.add(
							occupancy.getBuiltUpArea() == null ? BigDecimal.valueOf(0) : occupancy.getBuiltUpArea());
					existingBltUpArea = existingBltUpArea.add(occupancy.getExistingBuiltUpArea() == null
							? BigDecimal.valueOf(0) : occupancy.getExistingBuiltUpArea());
					flrArea = flrArea.add(occupancy.getFloorArea());
					existingFlrArea = existingFlrArea.add(occupancy.getExistingFloorArea());
					carpetArea = carpetArea.add(occupancy.getCarpetArea());
					existingCarpetArea = existingCarpetArea.add(occupancy.getExistingCarpetArea());
				}
			}
			/*
			 * This is hard coded for testing
			 */
			building.setTotalFloorArea(new BigDecimal(456));
			building.setTotalBuitUpArea(bltUpArea);
			building.setTotalExistingBuiltUpArea(existingBltUpArea);
			building.setTotalExistingFloorArea(existingFlrArea);

			// check block is completely existing building or not.
			if (existingBltUpArea.compareTo(bltUpArea) == 0)
				blk.setCompletelyExisting(Boolean.TRUE);

			totalFloorArea = totalFloorArea.add(flrArea);
			totalBuiltUpArea = totalBuiltUpArea.add(bltUpArea);
			totalExistingBuiltUpArea = totalExistingBuiltUpArea.add(existingBltUpArea);
			totalExistingFloorArea = totalExistingFloorArea.add(existingFlrArea);
			totalCarpetArea = totalCarpetArea.add(carpetArea);
			totalExistingCarpetArea = totalExistingCarpetArea.add(existingCarpetArea);

			// Find Occupancies by block and add
			Set<OccupancyTypeHelper> occupancyByBlock = new HashSet<>();
			for (Floor flr : building.getFloors()) {
				for (Occupancy occupancy : flr.getOccupancies()) {
					if (occupancy.getTypeHelper() != null)
						occupancyByBlock.add(occupancy.getTypeHelper());
				}
			}

			List<Map<String, Object>> listOfMapOfAllDtls = new ArrayList<>();
			List<OccupancyTypeHelper> listOfOccupancyTypes = new ArrayList<>();

			for (OccupancyTypeHelper occupancyType : occupancyByBlock) {

				Map<String, Object> allDtlsMap = new HashMap<>();
				BigDecimal blockWiseFloorArea = BigDecimal.ZERO;
				BigDecimal blockWiseBuiltupArea = BigDecimal.ZERO;
				BigDecimal blockWiseExistingFloorArea = BigDecimal.ZERO;
				BigDecimal blockWiseExistingBuiltupArea = BigDecimal.ZERO;
				for (Floor flr : blk.getBuilding().getFloors()) {
					for (Occupancy occupancy : flr.getOccupancies()) {
						if (occupancyType.getType() != null && occupancy.getTypeHelper() != null && occupancy
								.getTypeHelper().getType().getCode().equals(occupancyType.getType().getCode())) {
							blockWiseFloorArea = blockWiseFloorArea.add(occupancy.getFloorArea());
							blockWiseBuiltupArea = blockWiseBuiltupArea.add(occupancy.getBuiltUpArea() == null
									? BigDecimal.valueOf(0) : occupancy.getBuiltUpArea());
							blockWiseExistingFloorArea = blockWiseExistingFloorArea
									.add(occupancy.getExistingFloorArea());
							blockWiseExistingBuiltupArea = blockWiseExistingBuiltupArea
									.add(occupancy.getExistingBuiltUpArea() == null ? BigDecimal.valueOf(0)
											: occupancy.getExistingBuiltUpArea());

						}
					}
				}
				Occupancy occupancy = new Occupancy();
				occupancy.setBuiltUpArea(blockWiseBuiltupArea);
				occupancy.setFloorArea(blockWiseFloorArea);
				occupancy.setExistingFloorArea(blockWiseExistingFloorArea);
				occupancy.setExistingBuiltUpArea(blockWiseExistingBuiltupArea);
				occupancy.setCarpetArea(blockWiseFloorArea.multiply(BigDecimal.valueOf(.80)));
				occupancy.setTypeHelper(occupancyType);
				building.getTotalArea().add(occupancy);

				allDtlsMap.put("occupancy", occupancyType);
				allDtlsMap.put("totalFloorArea", blockWiseFloorArea);
				allDtlsMap.put("totalBuiltUpArea", blockWiseBuiltupArea);
				allDtlsMap.put("existingFloorArea", blockWiseExistingFloorArea);
				allDtlsMap.put("existingBuiltUpArea", blockWiseExistingBuiltupArea);

				listOfOccupancyTypes.add(occupancyType);

				listOfMapOfAllDtls.add(allDtlsMap);
			}
			Set<OccupancyTypeHelper> setOfOccupancyTypes = new HashSet<>(listOfOccupancyTypes);

			List<Occupancy> listOfOccupanciesOfAParticularblock = new ArrayList<>();
			// for each distinct converted occupancy types
			for (OccupancyTypeHelper occupancyType : setOfOccupancyTypes) {
				if (occupancyType != null) {
					Occupancy occupancy = new Occupancy();
					BigDecimal totalFlrArea = BigDecimal.ZERO;
					BigDecimal totalBltUpArea = BigDecimal.ZERO;
					BigDecimal totalExistingFlrArea = BigDecimal.ZERO;
					BigDecimal totalExistingBltUpArea = BigDecimal.ZERO;

					for (Map<String, Object> dtlsMap : listOfMapOfAllDtls) {
						if (occupancyType.equals(dtlsMap.get("occupancy"))) {
							totalFlrArea = totalFlrArea.add((BigDecimal) dtlsMap.get("totalFloorArea"));
							totalBltUpArea = totalBltUpArea.add((BigDecimal) dtlsMap.get("totalBuiltUpArea"));

							totalExistingBltUpArea = totalExistingBltUpArea
									.add((BigDecimal) dtlsMap.get("existingBuiltUpArea"));
							totalExistingFlrArea = totalExistingFlrArea
									.add((BigDecimal) dtlsMap.get("existingFloorArea"));

						}
					}
					occupancy.setTypeHelper(occupancyType);
					occupancy.setBuiltUpArea(totalBltUpArea);
					occupancy.setFloorArea(totalFlrArea);
					occupancy.setExistingBuiltUpArea(totalExistingBltUpArea);
					occupancy.setExistingFloorArea(totalExistingFlrArea);
					occupancy.setExistingCarpetArea(totalExistingFlrArea.multiply(BigDecimal.valueOf(0.80)));
					occupancy.setCarpetArea(totalFlrArea.multiply(BigDecimal.valueOf(0.80)));

					listOfOccupanciesOfAParticularblock.add(occupancy);
				}
			}
			blk.getBuilding().setOccupancies(listOfOccupanciesOfAParticularblock);

			if (!listOfOccupanciesOfAParticularblock.isEmpty()) {
				// listOfOccupanciesOfAParticularblock already converted. In
				// case of professional building type, converted into A1
				// type
				boolean singleFamilyBuildingTypeOccupancyPresent = false;
				boolean otherThanSingleFamilyOccupancyTypePresent = false;

				for (Occupancy occupancy : listOfOccupanciesOfAParticularblock) {
					if (occupancy.getTypeHelper().getSubtype() != null
							&& A_R.equals(occupancy.getTypeHelper().getSubtype().getCode()))
						singleFamilyBuildingTypeOccupancyPresent = true;
					else {
						otherThanSingleFamilyOccupancyTypePresent = true;
						break;
					}
				}
				blk.setSingleFamilyBuilding(
						!otherThanSingleFamilyOccupancyTypePresent && singleFamilyBuildingTypeOccupancyPresent);
				int allResidentialOccTypes = 0;
				int allResidentialOrCommercialOccTypes = 0;

				for (Occupancy occupancy : listOfOccupanciesOfAParticularblock) {
					if (occupancy.getTypeHelper() != null) {
						// setting residentialBuilding
						int residentialOccupancyType = 0;
						if (A.equals(occupancy.getTypeHelper().getType().getCode())) {
							residentialOccupancyType = 1;
						}
						if (residentialOccupancyType == 0) {
							allResidentialOccTypes = 0;
							break;
						} else {
							allResidentialOccTypes = 1;
						}
					}
				}
				blk.setResidentialBuilding(allResidentialOccTypes == 1);
				for (Occupancy occupancy : listOfOccupanciesOfAParticularblock) {
					if (occupancy.getTypeHelper() != null) {
						// setting residentialOrCommercial Occupancy Type
						int residentialOrCommercialOccupancyType = 0;
						if (A.equals(occupancy.getTypeHelper().getType().getCode())
								|| F.equals(occupancy.getTypeHelper().getType().getCode())) {
							residentialOrCommercialOccupancyType = 1;
						}
						if (residentialOrCommercialOccupancyType == 0) {
							allResidentialOrCommercialOccTypes = 0;
							break;
						} else {
							allResidentialOrCommercialOccTypes = 1;
						}
					}
				}
				blk.setResidentialOrCommercialBuilding(allResidentialOrCommercialOccTypes == 1);
			}

			if (blk.getBuilding().getFloors() != null && !blk.getBuilding().getFloors().isEmpty()) {
				BigDecimal noOfFloorsAboveGround = BigDecimal.ZERO;
				for (Floor floor : blk.getBuilding().getFloors()) {
					if (floor.getNumber() != null && floor.getNumber() >= 0) {
						noOfFloorsAboveGround = noOfFloorsAboveGround.add(BigDecimal.valueOf(1));
					}
				}

				boolean hasTerrace = blk.getBuilding().getFloors().stream()
						.anyMatch(floor -> floor.getTerrace().equals(Boolean.TRUE));

				noOfFloorsAboveGround = hasTerrace ? noOfFloorsAboveGround.subtract(BigDecimal.ONE)
						: noOfFloorsAboveGround;

				blk.getBuilding().setMaxFloor(noOfFloorsAboveGround);
				blk.getBuilding().setFloorsAboveGround(noOfFloorsAboveGround);
				blk.getBuilding().setTotalFloors(BigDecimal.valueOf(blk.getBuilding().getFloors().size()));
			}

		}

		for (Block blk : pl.getBlocks()) {
			Building building = blk.getBuilding();
			List<OccupancyTypeHelper> blockWiseOccupancyTypes = new ArrayList<>();
			for (Occupancy occupancy : blk.getBuilding().getOccupancies()) {
				if (occupancy.getTypeHelper() != null)
					blockWiseOccupancyTypes.add(occupancy.getTypeHelper());
			}
			Set<OccupancyTypeHelper> setOfBlockDistinctOccupancyTypes = new HashSet<>(blockWiseOccupancyTypes);
			OccupancyTypeHelper mostRestrictiveFar = getMostRestrictiveFar(setOfBlockDistinctOccupancyTypes);
			blk.getBuilding().setMostRestrictiveFarHelper(mostRestrictiveFar);

			for (Floor flr : building.getFloors()) {
				BigDecimal flrArea = BigDecimal.ZERO;
				BigDecimal existingFlrArea = BigDecimal.ZERO;
				BigDecimal carpetArea = BigDecimal.ZERO;
				BigDecimal existingCarpetArea = BigDecimal.ZERO;
				BigDecimal existingBltUpArea = BigDecimal.ZERO;
				for (Occupancy occupancy : flr.getOccupancies()) {
					flrArea = flrArea.add(occupancy.getFloorArea());
					existingFlrArea = existingFlrArea.add(occupancy.getExistingFloorArea());
					carpetArea = carpetArea.add(occupancy.getCarpetArea());
					existingCarpetArea = existingCarpetArea.add(occupancy.getExistingCarpetArea());
				}

				List<Occupancy> occupancies = flr.getOccupancies();
				for (Occupancy occupancy : occupancies) {
					existingBltUpArea = existingBltUpArea.add(occupancy.getExistingBuiltUpArea() != null
							? occupancy.getExistingBuiltUpArea() : BigDecimal.ZERO);
				}

				if (mostRestrictiveFar != null && mostRestrictiveFar.getConvertedSubtype() != null
						&& !A_R.equals(mostRestrictiveFar.getSubtype().getCode())) {
					if (carpetArea.compareTo(BigDecimal.ZERO) == 0) {
						pl.addError("Carpet area in block " + blk.getNumber() + "floor " + flr.getNumber(),
								"Carpet area is not defined in block " + blk.getNumber() + "floor " + flr.getNumber());
					}

					if (existingBltUpArea.compareTo(BigDecimal.ZERO) > 0
							&& existingCarpetArea.compareTo(BigDecimal.ZERO) == 0) {
						pl.addError("Existing Carpet area in block " + blk.getNumber() + "floor " + flr.getNumber(),
								"Existing Carpet area is not defined in block " + blk.getNumber() + "floor "
										+ flr.getNumber());
					}
				}

				if (flrArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
						.compareTo(carpetArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
								DcrConstants.ROUNDMODE_MEASUREMENTS)) < 0) {
					pl.addError("Floor area in block " + blk.getNumber() + "floor " + flr.getNumber(),
							"Floor area is less than carpet area in block " + blk.getNumber() + "floor "
									+ flr.getNumber());
				}

				if (existingBltUpArea.compareTo(BigDecimal.ZERO) > 0 && existingFlrArea
						.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
						.compareTo(existingCarpetArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
								DcrConstants.ROUNDMODE_MEASUREMENTS)) < 0) {
					pl.addError("Existing floor area in block " + blk.getNumber() + "floor " + flr.getNumber(),
							"Existing Floor area is less than carpet area in block " + blk.getNumber() + "floor "
									+ flr.getNumber());
				}
			}
		}

		List<OccupancyTypeHelper> plotWiseOccupancyTypes = new ArrayList<>();
		for (Block block : pl.getBlocks()) {
			for (Occupancy occupancy : block.getBuilding().getOccupancies()) {
				if (occupancy.getTypeHelper() != null)
					plotWiseOccupancyTypes.add(occupancy.getTypeHelper());
			}
		}

		Set<OccupancyTypeHelper> setOfDistinctOccupancyTypes = new HashSet<>(plotWiseOccupancyTypes);

		distinctOccupancyTypesHelper.addAll(setOfDistinctOccupancyTypes);

		List<Occupancy> occupanciesForPlan = new ArrayList<>();

		for (OccupancyTypeHelper occupancyType : setOfDistinctOccupancyTypes) {
			if (occupancyType != null) {
				BigDecimal totalFloorAreaForAllBlks = BigDecimal.ZERO;
				BigDecimal totalBuiltUpAreaForAllBlks = BigDecimal.ZERO;
				BigDecimal totalCarpetAreaForAllBlks = BigDecimal.ZERO;
				BigDecimal totalExistBuiltUpAreaForAllBlks = BigDecimal.ZERO;
				BigDecimal totalExistFloorAreaForAllBlks = BigDecimal.ZERO;
				BigDecimal totalExistCarpetAreaForAllBlks = BigDecimal.ZERO;
				Occupancy occupancy = new Occupancy();
				for (Block block : pl.getBlocks()) {
					for (Occupancy buildingOccupancy : block.getBuilding().getOccupancies()) {
						if (occupancyType.equals(buildingOccupancy.getTypeHelper())) {
							totalFloorAreaForAllBlks = totalFloorAreaForAllBlks.add(buildingOccupancy.getFloorArea());
							totalBuiltUpAreaForAllBlks = totalBuiltUpAreaForAllBlks
									.add(buildingOccupancy.getBuiltUpArea());
							totalCarpetAreaForAllBlks = totalCarpetAreaForAllBlks
									.add(buildingOccupancy.getCarpetArea());
							totalExistBuiltUpAreaForAllBlks = totalExistBuiltUpAreaForAllBlks
									.add(buildingOccupancy.getExistingBuiltUpArea());
							totalExistFloorAreaForAllBlks = totalExistFloorAreaForAllBlks
									.add(buildingOccupancy.getExistingFloorArea());
							totalExistCarpetAreaForAllBlks = totalExistCarpetAreaForAllBlks
									.add(buildingOccupancy.getExistingCarpetArea());
						}
					}
				}
				occupancy.setTypeHelper(occupancyType);
				occupancy.setBuiltUpArea(totalBuiltUpAreaForAllBlks);
				occupancy.setCarpetArea(totalCarpetAreaForAllBlks);
				occupancy.setFloorArea(totalFloorAreaForAllBlks);
				occupancy.setExistingBuiltUpArea(totalExistBuiltUpAreaForAllBlks);
				occupancy.setExistingFloorArea(totalExistFloorAreaForAllBlks);
				occupancy.setExistingCarpetArea(totalExistCarpetAreaForAllBlks);
				occupanciesForPlan.add(occupancy);
			}
		}

		pl.setOccupancies(occupanciesForPlan);
		pl.getVirtualBuilding().setTotalFloorArea(totalFloorArea);
		pl.getVirtualBuilding().setTotalCarpetArea(totalCarpetArea);
		pl.getVirtualBuilding().setTotalExistingBuiltUpArea(totalExistingBuiltUpArea);
		pl.getVirtualBuilding().setTotalExistingFloorArea(totalExistingFloorArea);
		pl.getVirtualBuilding().setTotalExistingCarpetArea(totalExistingCarpetArea);
		pl.getVirtualBuilding().setOccupancyTypes(distinctOccupancyTypesHelper);
		pl.getVirtualBuilding().setTotalBuitUpArea(totalBuiltUpArea);
		pl.getVirtualBuilding().setMostRestrictiveFarHelper(getMostRestrictiveFar(setOfDistinctOccupancyTypes));

		if (!distinctOccupancyTypesHelper.isEmpty()) {
			int allResidentialOccTypesForPlan = 0;
			for (OccupancyTypeHelper occupancy : distinctOccupancyTypesHelper) {
				LOG.info("occupancy :" + occupancy);
				// setting residentialBuilding
				int residentialOccupancyType = 0;
				if (A.equals(occupancy.getType().getCode())) {
					residentialOccupancyType = 1;
				}
				if (residentialOccupancyType == 0) {
					allResidentialOccTypesForPlan = 0;
					break;
				} else {
					allResidentialOccTypesForPlan = 1;
				}
			}
			pl.getVirtualBuilding().setResidentialBuilding(allResidentialOccTypesForPlan == 1);
			int allResidentialOrCommercialOccTypesForPlan = 0;
			for (OccupancyTypeHelper occupancyType : distinctOccupancyTypesHelper) {
				int residentialOrCommercialOccupancyTypeForPlan = 0;
				if (A.equals(occupancyType.getType().getCode()) || F.equals(occupancyType.getType().getCode())) {
					residentialOrCommercialOccupancyTypeForPlan = 1;
				}
				if (residentialOrCommercialOccupancyTypeForPlan == 0) {
					allResidentialOrCommercialOccTypesForPlan = 0;
					break;
				} else {
					allResidentialOrCommercialOccTypesForPlan = 1;
				}
			}
			pl.getVirtualBuilding().setResidentialOrCommercialBuilding(allResidentialOrCommercialOccTypesForPlan == 1);
		}
		if (!pl.getVirtualBuilding().getResidentialOrCommercialBuilding()) {
			pl.getErrors().put(DxfFileConstants.OCCUPANCY_ALLOWED_KEY, DxfFileConstants.OCCUPANCY_ALLOWED);
			return pl;
		}
		OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();
		BigDecimal providedFar = BigDecimal.ZERO;
		BigDecimal surrenderRoadArea = BigDecimal.ZERO;

		if (!pl.getSurrenderRoads().isEmpty()) {
			for (Measurement measurement : pl.getSurrenderRoads()) {
				surrenderRoadArea = surrenderRoadArea.add(measurement.getArea());
			}
		}

		pl.setTotalSurrenderRoadArea(surrenderRoadArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
				DcrConstants.ROUNDMODE_MEASUREMENTS));
		BigDecimal plotArea = pl.getPlot() != null ? pl.getPlot().getArea().add(surrenderRoadArea) : BigDecimal.ZERO;
		if (plotArea.doubleValue() > 0)
			providedFar = pl.getVirtualBuilding().getTotalFloorArea().divide(plotArea, DECIMALDIGITS_MEASUREMENTS,
					ROUNDMODE_MEASUREMENTS);

		pl.setFarDetails(new FarDetails());
		pl.getFarDetails().setProvidedFar(providedFar.doubleValue());
		String typeOfArea = pl.getPlanInformation().getTypeOfArea();
		BigDecimal roadWidth = pl.getPlanInformation().getRoadWidth();

	
			if ((mostRestrictiveOccupancyType.getType() != null
					&& DxfFileConstants.A.equalsIgnoreCase(mostRestrictiveOccupancyType.getType().getCode()))
					|| (mostRestrictiveOccupancyType.getSubtype() != null
							&& (A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())
									|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())))) {
				processFarResidential(pl, mostRestrictiveOccupancyType, providedFar, typeOfArea, roadWidth, errorMsgs);
			}
			 
		ProcessPrintHelper.print(pl);
		}catch(Exception e)	
		{	
		LOG.error(e,e);	
		}
		return pl;
	}

	private void decideNocIsRequired(Plan pl) {
		Boolean isHighRise = false;
		for (Block b : pl.getBlocks()) {
			if ((b.getBuilding() != null
					/*
					 * && b.getBuilding().getIsHighRise() != null &&
					 * b.getBuilding().getIsHighRise()
					 */ && b.getBuilding().getBuildingHeight().compareTo(new BigDecimal(5)) > 0)
					|| (b.getBuilding() != null && b.getBuilding().getCoverageArea() != null
							&& b.getBuilding().getCoverageArea().compareTo(new BigDecimal(500)) > 0)) {
				isHighRise = true;

			}
		}
		if (isHighRise) {
			pl.getPlanInformation().setNocFireDept("YES");
		}

		if (StringUtils.isNotBlank(pl.getPlanInformation().getBuildingNearMonument())
				&& "YES".equalsIgnoreCase(pl.getPlanInformation().getBuildingNearMonument())) {
			BigDecimal minDistanceFromMonument = BigDecimal.ZERO;
			List<BigDecimal> distancesFromMonument = pl.getDistanceToExternalEntity().getMonuments();
			if (!distancesFromMonument.isEmpty()) {

				minDistanceFromMonument = distancesFromMonument.stream().reduce(BigDecimal::min).get();

				if (minDistanceFromMonument.compareTo(BigDecimal.valueOf(300)) > 0) {
					pl.getPlanInformation().setNocNearMonument("YES");
				}
			}

		}

	}

	private void validate2(Plan pl, Block blk, Floor flr, Occupancy occupancy) {
		String occupancyTypeHelper = StringUtils.EMPTY;
		if (occupancy.getTypeHelper() != null) {
			if (occupancy.getTypeHelper().getType() != null) {
				occupancyTypeHelper = occupancy.getTypeHelper().getType().getName();
			} else if (occupancy.getTypeHelper().getSubtype() != null) {
				occupancyTypeHelper = occupancy.getTypeHelper().getSubtype().getName();
			}
		}

		if (occupancy.getBuiltUpArea() != null && occupancy.getBuiltUpArea().compareTo(BigDecimal.valueOf(0)) < 0) {
			pl.addError(VALIDATION_NEGATIVE_BUILTUP_AREA, getLocaleMessage(VALIDATION_NEGATIVE_BUILTUP_AREA,
					blk.getNumber(), flr.getNumber().toString(), occupancyTypeHelper));
		}
		if (occupancy.getExistingBuiltUpArea() != null
				&& occupancy.getExistingBuiltUpArea().compareTo(BigDecimal.valueOf(0)) < 0) {
			pl.addError(VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA,
					getLocaleMessage(VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA, blk.getNumber(),
							flr.getNumber().toString(), occupancyTypeHelper));
		}
		occupancy.setFloorArea((occupancy.getBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getBuiltUpArea())
				.subtract(occupancy.getDeduction() == null ? BigDecimal.ZERO : occupancy.getDeduction()));
		if (occupancy.getFloorArea() != null && occupancy.getFloorArea().compareTo(BigDecimal.valueOf(0)) < 0) {
			pl.addError(VALIDATION_NEGATIVE_FLOOR_AREA, getLocaleMessage(VALIDATION_NEGATIVE_FLOOR_AREA,
					blk.getNumber(), flr.getNumber().toString(), occupancyTypeHelper));
		}
		occupancy.setExistingFloorArea(
				(occupancy.getExistingBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getExistingBuiltUpArea())
						.subtract(occupancy.getExistingDeduction() == null ? BigDecimal.ZERO
								: occupancy.getExistingDeduction()));
		if (occupancy.getExistingFloorArea() != null
				&& occupancy.getExistingFloorArea().compareTo(BigDecimal.valueOf(0)) < 0) {
			pl.addError(VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA,
					getLocaleMessage(VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA, blk.getNumber(),
							flr.getNumber().toString(), occupancyTypeHelper));
		}
	}

	protected OccupancyTypeHelper getMostRestrictiveFar(Set<OccupancyTypeHelper> distinctOccupancyTypes) {
		Set<String> codes = new HashSet<>();
		Map<String, OccupancyTypeHelper> codesMap = new HashMap<>();
		for (OccupancyTypeHelper typeHelper : distinctOccupancyTypes) {

			if (typeHelper.getType() != null)
				codesMap.put(typeHelper.getType().getCode(), typeHelper);
			if (typeHelper.getSubtype() != null)
				codesMap.put(typeHelper.getSubtype().getCode(), typeHelper);
		}
		codes = codesMap.keySet();
		if (codes.contains(S_ECFG))
			return codesMap.get(S_ECFG);
		else if (codes.contains(A_FH))
			return codesMap.get(A_FH);
		else if (codes.contains(S_SAS))
			return codesMap.get(S_SAS);
		else if (codes.contains(D_B))
			return codesMap.get(D_B);
		else if (codes.contains(D_C))
			return codesMap.get(D_C);
		else if (codes.contains(D_A))
			return codesMap.get(D_A);
		else if (codes.contains(H_PP))
			return codesMap.get(H_PP);
		else if (codes.contains(E_NS))
			return codesMap.get(E_NS);
		else if (codes.contains(M_DFPAB))
			return codesMap.get(M_DFPAB);
		else if (codes.contains(E_PS))
			return codesMap.get(E_PS);
		else if (codes.contains(E_SFMC))
			return codesMap.get(E_SFMC);
		else if (codes.contains(E_SFDAP))
			return codesMap.get(E_SFDAP);
		else if (codes.contains(E_EARC))
			return codesMap.get(E_EARC);
		else if (codes.contains(S_MCH))
			return codesMap.get(S_MCH);
		else if (codes.contains(S_BH))
			return codesMap.get(S_BH);
		else if (codes.contains(S_CRC))
			return codesMap.get(S_CRC);
		else if (codes.contains(S_CA))
			return codesMap.get(S_CA);
		else if (codes.contains(S_SC))
			return codesMap.get(S_SC);
		else if (codes.contains(S_ICC))
			return codesMap.get(S_ICC);
		else if (codes.contains(A2))
			return codesMap.get(A2);
		else if (codes.contains(E_CLG))
			return codesMap.get(E_CLG);
		else if (codes.contains(M_OHF))
			return codesMap.get(M_OHF);
		else if (codes.contains(M_VH))
			return codesMap.get(M_VH);
		else if (codes.contains(M_NAPI))
			return codesMap.get(M_NAPI);
		else if (codes.contains(A_SA))
			return codesMap.get(A_SA);
		else if (codes.contains(M_HOTHC))
			return codesMap.get(M_HOTHC);
		else if (codes.contains(E_SACA))
			return codesMap.get(E_SACA);
		else if (codes.contains(F))
			return codesMap.get(F);
		else if (codes.contains(A))
			return codesMap.get(A);
		else
			return null;

	}

	//pondy customization

	private void processFarResidential(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal far, String typeOfArea,
			BigDecimal roadWidth, HashMap<String, String> errors) {

		String ruleDesc = StringUtils.EMPTY;
		String expectedResult = StringUtils.EMPTY;
		String plotType=REGULAR;
		String buildingType=REGULAR;
		String	crzType="";
		boolean isAccepted = false;
	// getting additoinal property crz_area
		String crz = pl.getPlanInfoProperties().get("CRZ_AREA");
		// check it is ews
		Boolean ewsPlot = isEwsPlot(pl);
		Boolean ewsBuilding = isEwsBuilding(pl);
		Boolean CRZZone=false;
		if(ewsPlot)
			plotType="EWS";
		if(ewsBuilding)
			buildingType="EWS";
		String crzValue = pl.getPlanInfoProperties().get(DxfFileConstants.CRZ_ZONE);
	
		LOG.info("CRZ="+pl.getPlanInformation().getCrzZoneArea());
		if(crzValue!=null && crzValue.equalsIgnoreCase(DcrConstants.YES))
		{
			CRZZone=true;
		} 
		if (CRZZone) {
			crzType=crz;
			}

		if (typeOfArea.equalsIgnoreCase(TAMIL_TOWN)) {
			
			if (CRZZone) {
				switch (crz) {
				case CRZ1:
					pl.addError("Not Implemented","No Data for CRZ-I under Tamil Town" );	
					break;

				case CRZ2:
					isAccepted = far.compareTo(ONE_POINTFIVE) <= 0;
					pl.getFarDetails().setPermissableFar(ONE_POINTFIVE.doubleValue());
					expectedResult = "<=" + ONE_POINTFIVE;
					ruleDesc=RULE_CRZ_II;
					break;
				case CRZ3:
					
					pl.addError("Not Implemented","No Data for CRZ- III under Tamil Town" );
					break;

				}
			} else {
				if (ewsPlot) {
					isAccepted = far.compareTo(TWO_TWO) <= 0;
					pl.getFarDetails().setPermissableFar(TWO_TWO.doubleValue());
					expectedResult = "<=" + TWO_TWO;
				} else if (ewsBuilding) {
					isAccepted = far.compareTo(THREE) <= 0;
					pl.getFarDetails().setPermissableFar(THREE.doubleValue());
					expectedResult = "<=" + THREE;
				} else {
					isAccepted = far.compareTo(THREE) <= 0;
					pl.getFarDetails().setPermissableFar(THREE.doubleValue());
					expectedResult = "<=" + THREE;
				}
				ruleDesc=RULE_38;
			}
		}
		else if (typeOfArea.equalsIgnoreCase(WHITE_TOWN)) {
			
			if (CRZZone) {
				switch (crz) {
				case CRZ1:
					pl.addError("Not Implemented","No Data for CRZ-I under White Town" );	
					break;

				case CRZ2:
						isAccepted = far.compareTo(ONE_POINTTWO) <= 0;
						pl.getFarDetails().setPermissableFar(ONE_POINTTWO.doubleValue());
						expectedResult = "<=" + ONE_POINTTWO;
						ruleDesc=RULE_CRZ_II;
					break;
				case CRZ3:
					
					pl.addError("Not Implemented","No Data for CRZ- III under White Town" );
					break;

				}
			}
		}
		else if (typeOfArea.equalsIgnoreCase(OUTSIDE_BOULEVARD)) {
			
			if (CRZZone) {
				switch (crz) {
				case CRZ1:
					pl.addError("Not Implemented","No Data for CRZ-I under Tamil Town" );	
					break;

				case CRZ2:
					isAccepted = far.compareTo(ONE_POINTFIVE) <= 0;
					pl.getFarDetails().setPermissableFar(ONE_POINTFIVE.doubleValue());
					expectedResult = "<=" + ONE_POINTFIVE;
					ruleDesc=RULE_CRZ_II;
					break;
				case CRZ3:
					
					isAccepted = far.compareTo(TWO_TWO) <= 0;
					pl.getFarDetails().setPermissableFar(TWO_TWO.doubleValue());
					expectedResult = "<=" + TWO_TWO;
					ruleDesc=RULE_38;
					break;

				}
			} else {
				isAccepted = far.compareTo(TWO_TWO) <= 0;
				pl.getFarDetails().setPermissableFar(TWO_TWO.doubleValue());
				expectedResult = "<=" + TWO_TWO;
				ruleDesc=RULE_38;
			}
		}
		else if (typeOfArea.equalsIgnoreCase(OTHER_AREA)) {
			
			if (CRZZone) {
				switch (crz) {
				case CRZ1:
					pl.addError("Not Implemented","No Data for CRZ-I under Tamil Town" );	
					break;

				case CRZ2:
					isAccepted = far.compareTo(ONE_POINTFIVE) <= 0;
					pl.getFarDetails().setPermissableFar(ONE_POINTFIVE.doubleValue());
					expectedResult = "<=" + ONE_POINTFIVE;
					ruleDesc=RULE_CRZ_II;
					break;
				case CRZ3:
					
					isAccepted = far.compareTo(TWO_TWO) <= 0;
					pl.getFarDetails().setPermissableFar(TWO_TWO.doubleValue());
					expectedResult = "<=" + TWO_TWO;
					ruleDesc=RULE_38;
					break;

				}
			} else {
				isAccepted = far.compareTo(TWO_TWO) <= 0;
				pl.getFarDetails().setPermissableFar(TWO_TWO.doubleValue());
				expectedResult = "<=" + TWO_TWO;
				ruleDesc=RULE_38;
			}
		}
		
		if (errors.isEmpty() && StringUtils.isNotBlank(expectedResult)) {
			buildResult(pl, occupancyType, far, typeOfArea,plotType ,buildingType, crzType,expectedResult, isAccepted,ruleDesc);
		}

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



	 
	
	private void buildResult(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal far, String typeOfArea,
			String plotType,String buildingType, String crzArea, String expectedResult, boolean isAccepted,String ruleDesc) {
		ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.addColumnHeading(1, RULE_NO);
		scrutinyDetail.addColumnHeading(2, OCCUPANCY);
		scrutinyDetail.addColumnHeading(3, AREA_TYPE);
		scrutinyDetail.addColumnHeading(4, PLOT_TYPE);
		scrutinyDetail.addColumnHeading(5, BUILDING_TYPE);
		scrutinyDetail.addColumnHeading(6, CRZ_AREA);
		scrutinyDetail.addColumnHeading(7, PERMISSIBLE);
		scrutinyDetail.addColumnHeading(8, PROVIDED);
		scrutinyDetail.addColumnHeading(9, STATUS);
		scrutinyDetail.setKey("Common_FAR");

		String actualResult = far.toString();
		String occupancyName;
		if (occupancyType.getSubtype() != null)
			occupancyName = occupancyType.getSubtype().getName();
		else
			occupancyName = occupancyType.getType().getName();

		Map<String, String> details = new HashMap<>();
		details.put(RULE_NO, ruleDesc);
		details.put(OCCUPANCY, occupancyName);
		details.put(AREA_TYPE, typeOfArea);
		details.put(PLOT_TYPE, plotType.toString());
		details.put(BUILDING_TYPE, buildingType.toString());
		details.put(CRZ_AREA, crzArea.toString());
		details.put(PERMISSIBLE, expectedResult);
		details.put(PROVIDED, actualResult);
		details.put(STATUS, isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

		scrutinyDetail.getDetail().add(details);
		pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	}

	 

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}
}
