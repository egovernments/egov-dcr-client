package org.egov.client.edcr.feature;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.SetBack;
import org.egov.edcr.feature.FeatureProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetBackService_Pondicherry extends FeatureProcess
{
  @Autowired
  private FrontYardService_Pondicherry frontYardService;
  @Autowired
  private SideYardService_Pondicherry sideYardService;
  @Autowired
  private RearYardService_Pondicherry rearYardService;
  
  public Plan validate(Plan pl) {
    HashMap<String, String> errors = new HashMap<String, String>();
    
    BigDecimal heightOfBuilding = BigDecimal.ZERO;
    for (Block block : pl.getBlocks()) {
      heightOfBuilding = block.getBuilding().getBuildingHeight();
      int i = 0;
      if (!block.getCompletelyExisting().booleanValue())
        for (SetBack setback : block.getSetBacks()) {
          i++;
          if (setback.getLevel().intValue() == 0) {
            if (setback.getFrontYard() == null)
              errors.put("frontyardNodeDefined", 
                  getLocaleMessage("msg.error.not.defined", new String[] { " SetBack of " + block.getName() + "  at level zero " })); 
            if (setback.getRearYard() == null && 
              !pl.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase("YES"))
              errors.put("rearyardNodeDefined", 
                  getLocaleMessage("msg.error.not.defined", new String[] { " Rear Setback of  " + block.getName() + "  at level zero " })); 
            if (setback.getSideYard1() == null)
              errors.put("side1yardNodeDefined", getLocaleMessage("msg.error.not.defined", new String[] { " Side Setback 1 of block " + block
                      .getName() + " at level zero" })); 
            if (setback.getSideYard2() == null && 
              !pl.getPlanInformation().getNocToAbutSideDesc().equalsIgnoreCase("YES"))
              errors.put("side2yardNodeDefined", getLocaleMessage("msg.error.not.defined", new String[] { " Side Setback 2 of block " + block
                      .getName() + " at level zero " })); 
          } 
          else if (setback.getLevel().intValue() > 0) {
            if (setback.getFrontYard() != null && setback.getFrontYard().getHeight() == null)
              errors.put("frontyardnotDefinedHeight", getLocaleMessage("msg.height.notdefined", new String[] { "Front Setback ", block
                      .getName(), setback.getLevel().toString() })); 
            if (setback.getRearYard() != null && setback.getRearYard().getHeight() == null)
              errors.put("rearyardnotDefinedHeight", getLocaleMessage("msg.height.notdefined", new String[] { "Rear Setback ", block
                      .getName(), setback.getLevel().toString() })); 
            if (setback.getSideYard1() != null && setback.getSideYard1().getHeight() == null)
              errors.put("side1yardnotDefinedHeight", getLocaleMessage("msg.height.notdefined", new String[] { "Side Setback 1 ", block
                      .getName(), setback.getLevel().toString() })); 
            if (setback.getSideYard2() != null && setback.getSideYard2().getHeight() == null) {
              errors.put("side2yardnotDefinedHeight", getLocaleMessage("msg.height.notdefined", new String[] { "Side Setback 2 ", block
                      .getName(), setback.getLevel().toString() }));
            }
          }
          
          if (setback.getLevel().intValue() > 0 && block.getSetBacks().size() == i) {
            if (setback.getFrontYard() != null && setback.getFrontYard().getHeight() != null && setback
              .getFrontYard().getHeight().compareTo(heightOfBuilding) != 0)
              errors.put("frontyardDefinedWrongHeight", getLocaleMessage("msg.wrong.height.defined", new String[] { "Front Setback ", block
                      .getName(), setback.getLevel().toString(), heightOfBuilding.toString() })); 
            if (setback.getRearYard() != null && setback.getRearYard().getHeight() != null && setback
              .getRearYard().getHeight().compareTo(heightOfBuilding) != 0)
              errors.put("rearyardDefinedWrongHeight", getLocaleMessage("msg.wrong.height.defined", new String[] { "Rear Setback ", block
                      .getName(), setback.getLevel().toString(), heightOfBuilding.toString() })); 
            if (setback.getSideYard1() != null && setback.getSideYard1().getHeight() != null && setback
              .getSideYard1().getHeight().compareTo(heightOfBuilding) != 0)
              errors.put("side1yardDefinedWrongHeight", getLocaleMessage("msg.wrong.height.defined", new String[] { "Side Setback 1 ", block
                      .getName(), setback.getLevel().toString(), heightOfBuilding.toString() })); 
            if (setback.getSideYard2() != null && setback.getSideYard2().getHeight() != null && setback
              .getSideYard2().getHeight().compareTo(heightOfBuilding) != 0) {
              errors.put("side2yardDefinedWrongHeight", getLocaleMessage("msg.wrong.height.defined", new String[] { "Side Setback 2 ", block
                      .getName(), setback.getLevel().toString(), heightOfBuilding.toString() }));
            }
          } 
        }  
    } 
    if (errors.size() > 0) {
      pl.addErrors(errors);
    }
    return pl;
  }

  public Plan process(Plan pl) {
    validate(pl);
    
    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
    if (depthOfPlot != null && depthOfPlot.compareTo(BigDecimal.ZERO) > 0) {
      this.frontYardService.processFrontYard(pl);
      this.rearYardService.processRearYard(pl);
      this.sideYardService.processSideYard(pl);
    } 
    return pl;
  }

  public Map<String, Date> getAmendments() { 
	  return new LinkedHashMap();
  }
}
