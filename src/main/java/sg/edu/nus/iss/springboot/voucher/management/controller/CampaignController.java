package sg.edu.nus.iss.springboot.voucher.management.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.springboot.voucher.management.dto.APIResponse;
import sg.edu.nus.iss.springboot.voucher.management.dto.CampaignDTO;
import sg.edu.nus.iss.springboot.voucher.management.entity.Campaign;
import sg.edu.nus.iss.springboot.voucher.management.service.impl.CampaignService;

@RestController
@Validated
@RequestMapping("/api/campaign")
public class CampaignController {

    private static final Logger logger = LoggerFactory.getLogger(CampaignController.class);

    @Autowired
    private CampaignService campaignService;

    @GetMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<APIResponse<List<CampaignDTO>>> getAllActiveStore() {
        try {
            logger.info("Calling Campaign getALL API...");
            return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(campaignService.findAllCampaigns(), "Successfully get all campaigns"));
        } catch (Exception ex) {
            logger.info("Calling Campaign getALL API failed...");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(campaignService.findAllCampaigns(), "Failed to get all campaigns"));
        }
    }

    @GetMapping(value = "/campaign/{id}", produces = "application/json")
    public ResponseEntity<APIResponse<CampaignDTO>> getByCampaignId(@PathVariable String campaignId) {
        try {
            logger.info("Calling get Campaign API...");
            return ResponseEntity.status(HttpStatus.OK)
                .body(APIResponse.success(campaignService.findByCampaignId(campaignId), "Successfully get campaignId "+ campaignId));
        } catch (Exception ex) {
            logger.error("Calling Campaign get Campaign API failed...");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.success(campaignService.findByCampaignId(campaignId), "Failed to get campaignId "+ campaignId));
        }
        
    }

    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<APIResponse<CampaignDTO>> createCampaign(@RequestPart("campaign") Campaign campaign) {
        try {
            logger.info("Calling Campaign create API...");
            return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(campaignService.create(campaign), "Created sucessfully"));
        } catch (Exception ex) {
            logger.error("Calling Campaign create API failed...");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(campaignService.create(campaign), "Created failed"));
        }
    }

    @PostMapping(value = "/update", produces = "application/json")
    public ResponseEntity<APIResponse<CampaignDTO>> updateCampaign(@RequestPart("campaign") Campaign campaign) {
        try {
            logger.info("Calling Campaign update API...");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success(campaignService.update(campaign), "Updated sucessfully"));
        } catch (Exception ex) {
            logger.info("Calling Campaign update API failed...");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(campaignService.update(campaign), "Updated failed"));
        }
    }

    @PostMapping(value = "/delete/{campaignId}", produces = "application/json")
    public ResponseEntity<APIResponse<Campaign>> deleteCampaign(@PathVariable String campaignId) {
        try {
            logger.info("Calling Campaign delete API...");
            campaignService.delete(campaignId);
            return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success("Deleted successfully"));
        } catch (Exception ex) {
            logger.error("Calling Campaign delete API failed...");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error("Delete failed"));
        }
    }
}
