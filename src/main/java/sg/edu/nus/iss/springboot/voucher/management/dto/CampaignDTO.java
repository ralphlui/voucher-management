package sg.edu.nus.iss.springboot.voucher.management.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import sg.edu.nus.iss.springboot.voucher.management.enums.CampaignStatus;

@Data
@AllArgsConstructor
public class CampaignDTO {

    private String campaignId;
	private String description;
    private StoreDTO store;
	private CampaignStatus campaignStatus;
	private String tagsJson;
	private int numberOfVouchers;
	private int numberOfLikes;
	private String pin;
	private String condition1;
	private String condition2;
    private UserDTO createdBy;
    private UserDTO updatedBy;
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;

    public CampaignDTO(){
    }

    public String getCampaignId() {
        return this.campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StoreDTO getStore() {
        return this.store;
    }

    public void setStore(StoreDTO store) {
        this.store = store;
    }

    public CampaignStatus getCampaignStatus() {
        return this.campaignStatus;
    }

    public void setCampaignStatus(CampaignStatus campaignStatus) {
        this.campaignStatus = campaignStatus;
    }

    public String getTagsJson() {
        return this.tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public int getNumberOfVouchers() {
        return this.numberOfVouchers;
    }

    public void setNumberOfVouchers(int numberOfVouchers) {
        this.numberOfVouchers = numberOfVouchers;
    }

    public int getNumberOfLikes() {
        return this.numberOfLikes;
    }

    public void setNumberOfLikes(int numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    public String getPin() {
        return this.pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getCondition1() {
        return this.condition1;
    }

    public void setCondition1(String condition1) {
        this.condition1 = condition1;
    }

    public String getCondition2() {
        return this.condition2;
    }

    public void setCondition2(String condition2) {
        this.condition2 = condition2;
    }

    public UserDTO getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public UserDTO getUpdatedBy() {
        return this.updatedBy;
    }

    public void setUpdatedBy(UserDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return this.updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

}