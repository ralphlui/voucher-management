package sg.edu.nus.iss.springboot.voucher.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.nus.iss.springboot.voucher.management.entity.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, String>{
   
}