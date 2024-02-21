package sg.edu.nus.iss.springboot.voucher.management.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import sg.edu.nus.iss.springboot.voucher.management.configuration.VourcherManagementSecurityConfig;
import sg.edu.nus.iss.springboot.voucher.management.entity.RoleType;
import sg.edu.nus.iss.springboot.voucher.management.entity.Store;
import sg.edu.nus.iss.springboot.voucher.management.entity.User;
import sg.edu.nus.iss.springboot.voucher.management.model.StoreResponse;
import sg.edu.nus.iss.springboot.voucher.management.model.StoreResponse.ResultStore;
import sg.edu.nus.iss.springboot.voucher.management.model.StoreResponseDetail;
import sg.edu.nus.iss.springboot.voucher.management.model.StoreResponseDetail.ResultStoreDetail;
import sg.edu.nus.iss.springboot.voucher.management.service.impl.StoreService;
import sg.edu.nus.iss.springboot.voucher.management.service.impl.UserService;
import sg.edu.nus.iss.springboot.voucher.management.utility.GeneralUtility;
import sg.edu.nus.iss.springboot.voucher.management.utility.ImageUploadToS3;

@RestController
@RequestMapping("/api/store")
public class StoreController {

	private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

	@Autowired
	private StoreService storeService;

	@Autowired
	private UserService userService;

	@Autowired
	private AmazonS3 s3Client;

	@Autowired
	private VourcherManagementSecurityConfig securityConfig;

	@GetMapping(value = "/getAll", produces = "application/json")
	public ResponseEntity<StoreResponseDetail> getAllStore() {
		logger.info("Call store getAll API...");

		ArrayList<ResultStoreDetail> resultList = new ArrayList<ResultStoreDetail>();
		StoreResponseDetail storeResponse = new StoreResponseDetail();
		try {
			List<Store> storeList = storeService.findByIsDeletedFalse();

			if (storeList.isEmpty()) {
				storeResponse.setMessage("No Store found.");
				storeResponse.setResult(resultList);
				return new ResponseEntity<>(storeResponse, HttpStatus.OK);
			} else {

				storeResponse.setMessage(HttpStatus.OK + "");

				for (Store store : storeList) {
					ResultStoreDetail result = new ResultStoreDetail();
					result.setStoreId(store.getStoreId());
					result.setStoreName(store.getStoreName());
					result.setDescription(GeneralUtility.makeNotNull(store.getDescription()));

					String address = "";
					String address1 = GeneralUtility.makeNotNull(store.getAddress1()).trim();
					String address2 = GeneralUtility.makeNotNull(store.getAddress2()).trim();
					String address3 = GeneralUtility.makeNotNull(store.getAddress3()).trim();
					String postalCode = GeneralUtility.makeNotNull(store.getPostalCode());
					if (!address1.isEmpty()) {
						address = address.concat(address1).concat(",");
					}

					if (!address2.isEmpty()) {
						address = address.concat(address2).concat(",");
					}

					if (!address3.isEmpty()) {
						address = address.concat(address3).concat(",");
					}

					if (!postalCode.isEmpty()) {
						address = address.concat(postalCode);
					}

					result.setAddress(GeneralUtility.makeNotNull(address));
					result.setAddress1(address1);
					result.setAddress2(address2);
					result.setAddress3(address3);
					result.setCity(GeneralUtility.makeNotNull(store.getCity()));
					result.setState(GeneralUtility.makeNotNull(store.getState()));
					result.setCountry(GeneralUtility.makeNotNull(store.getCountry()));
					result.setContactNumber(GeneralUtility.makeNotNull(store.getContactNumber()));
					result.setPostalCode(postalCode);

					String imageUrl = GeneralUtility.makeNotNull(store.getImage());
					if (!imageUrl.equals("")) {

						String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

						boolean isImageExists = s3Client.doesObjectExist(securityConfig.getS3Bucket(),
								securityConfig.getS3ImagePublic().trim() + fileName.trim());

						if (isImageExists) {

							String presignedUrl = GeneralUtility
									.makeNotNull(ImageUploadToS3.generatePresignedUrl(s3Client, securityConfig,
											securityConfig.getS3ImagePublic().trim() + fileName.trim()));

							result.setImage(presignedUrl);
						}
					} else {
						result.setImage("");
					}

					resultList.add(result);

				}

				storeResponse.setResult(resultList);

				return new ResponseEntity<>(storeResponse, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("Error, " + e.toString());
			storeResponse.setMessage("Error," + e.toString());
			storeResponse.setResult(resultList);
			return new ResponseEntity<>(storeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/create", produces = "application/json")
	public ResponseEntity<StoreResponse> createStore(@RequestPart("store") Store store,
			@RequestPart(value = "image", required = false) MultipartFile uploadFile) {
		logger.info("Call store create API...");

		String message = "";

		ArrayList<ResultStore> resultList = new ArrayList<ResultStore>();

		StoreResponse storeResponse = new StoreResponse();
		ResultStore result = new ResultStore();
		try {
			if (!GeneralUtility.makeNotNull(store.getCreatedBy()).equals("")) {

				// Check for user role.
				String email = GeneralUtility.makeNotNull(store.getCreatedBy()).trim();
				logger.info("Store create user: " + email);
				User user = userService.findByEmail(email);

				if (!GeneralUtility.makeNotNull(user).equals("")) {
					String userRole = GeneralUtility.makeNotNull(user.getRole()).trim();

					logger.info("Store create user role: " + userRole);

					if (userRole.equals(RoleType.MERCHANT.toString())) {

						if (!GeneralUtility.makeNotNull(store.getStoreName()).equals("")) {
							Store dbStore = storeService.findByStoreName(store.getStoreName());

							if (GeneralUtility.makeNotNull(dbStore).equals("")) {

								store.setCreatedBy(user.getUserId());

								if (!GeneralUtility.makeNotNull(uploadFile).equals("")) {
									logger.info("create store: " + store.getStoreName() + "::"
											+ uploadFile.getOriginalFilename());

									boolean isImageUploaded = ImageUploadToS3.checkImageExistBeforeUpload(s3Client,
											uploadFile, securityConfig, securityConfig.getS3ImagePublic().trim(), true);
									if (isImageUploaded) {
										String imageUrl = securityConfig.getS3ImageUrlPrefix().trim() + "/"
												+ securityConfig.getS3ImagePublic().trim()
												+ uploadFile.getOriginalFilename().trim();
										store.setImage(imageUrl);
									}

								}

								Store createdStore = storeService.create(store);
								if (!GeneralUtility.makeNotNull(createdStore).equals("")) {

									message = "Store created successfully.";
									logger.info(message + store.getStoreName());
									result.setStoreId(createdStore.getStoreId());
									result.setStoreName(createdStore.getStoreName());
									resultList.add(result);
									storeResponse.setMessage(message);
									storeResponse.setResult(resultList);

									return new ResponseEntity<>(storeResponse, HttpStatus.OK);

								} else {
									message = "Create store failed: Unable to create a new store with :"
											+ store.getStoreName();
									logger.error(message);
									resultList.add(result);
									storeResponse.setMessage(message);
									storeResponse.setResult(new ArrayList<ResultStore>());
									return new ResponseEntity<>(storeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
								}

							} else {

								message = "Store already exists.";
								logger.error(message);
								storeResponse.setMessage(message);
								storeResponse.setResult(resultList);
								return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
							}
						} else {
							message = "Bad Request:Store name could not be blank.";
							logger.error(message);
							storeResponse.setMessage(message);
							storeResponse.setResult(resultList);
							return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
						}

					} else {
						message = "Bad Request: Invalid user role.";
						logger.error(message);
						storeResponse.setMessage(message);
						storeResponse.setResult(resultList);
						return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
					}

				} else {
					message = "Store create failed: Unable to find the user with email :" + store.getCreatedBy();
					logger.error(message);
					storeResponse.setMessage(message);
					storeResponse.setResult(resultList);
					return new ResponseEntity<>(storeResponse, HttpStatus.NOT_FOUND);
				}

			} else {
				message = "Bad Request: Store Create user field could not be blank.";
				logger.error(message);
				storeResponse.setMessage(message);
				storeResponse.setResult(resultList);
				return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error, " + e.toString());
			storeResponse.setMessage("Error, " + e.toString());
			storeResponse.setResult(resultList);
			return new ResponseEntity<>(storeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/update", produces = "application/json")
	public ResponseEntity<StoreResponse> updateStore(@RequestPart("store") Store store,
			@RequestPart(value = "image", required = false) MultipartFile uploadFile) {
		logger.info("Call store update API...");

		String message = "";

		ArrayList<ResultStore> resultList = new ArrayList<ResultStore>();

		StoreResponse storeResponse = new StoreResponse();
		ResultStore result = new ResultStore();
		try {

			if (!GeneralUtility.makeNotNull(store.getStoreId()).equals("")) {

				//
				Optional<Store> dbStore = storeService.findById(GeneralUtility.makeNotNull(store.getStoreId()));

				if (dbStore.isPresent()) {
					//
					if (!GeneralUtility.makeNotNull(store.getUpdatedBy()).equals("")) {

						// Check for user role.
						String email = GeneralUtility.makeNotNull(store.getUpdatedBy()).trim();
						logger.info("Store update user: " + email);
						User user = userService.findByEmail(email);

						if (!GeneralUtility.makeNotNull(user).equals("")) {
							String userRole = GeneralUtility.makeNotNull(user.getRole()).trim();

							logger.info("Store update user role: " + userRole);

							if (userRole.equals(RoleType.MERCHANT.toString())) {

								if (!GeneralUtility.makeNotNull(store.getStoreName()).equals("")) {

									dbStore.get().setUpdatedBy(user.getUserId());

									if (!GeneralUtility.makeNotNull(uploadFile).equals("")) {
										logger.info("update store: " + store.getStoreName() + "::"
												+ uploadFile.getOriginalFilename());

										boolean isImageUploaded = ImageUploadToS3.checkImageExistBeforeUpload(s3Client,
												uploadFile, securityConfig, securityConfig.getS3ImagePublic().trim(),
												true);
										if (isImageUploaded) {
											String imageUrl = securityConfig.getS3ImageUrlPrefix().trim() + "/"
													+ securityConfig.getS3ImagePublic().trim()
													+ uploadFile.getOriginalFilename().trim();
											dbStore.get().setImage(GeneralUtility.makeNotNull(imageUrl));
										}

									}

									dbStore.get().setDescription(GeneralUtility.makeNotNull(store.getDescription()));
									dbStore.get().setAddress1(GeneralUtility.makeNotNull(store.getAddress1()));
									dbStore.get().setAddress2(GeneralUtility.makeNotNull(store.getAddress2()));
									dbStore.get().setAddress3(GeneralUtility.makeNotNull(store.getAddress3()));
									dbStore.get().setCity(GeneralUtility.makeNotNull(store.getCity()));
									dbStore.get().setState(GeneralUtility.makeNotNull(store.getState()));
									dbStore.get().setCountry(GeneralUtility.makeNotNull(store.getCountry()));
									dbStore.get()
											.setContactNumber(GeneralUtility.makeNotNull(store.getContactNumber()));
									dbStore.get().setDeleted(store.isDeleted());

									Store updatedStore = storeService.update(dbStore.get());
									if (!GeneralUtility.makeNotNull(updatedStore).equals("")) {

										message = "Store updated successfully.";
										logger.info(message + store.getStoreName());
										result.setStoreId(updatedStore.getStoreId());
										result.setStoreName(updatedStore.getStoreName());
										resultList.add(result);
										storeResponse.setMessage(message);
										storeResponse.setResult(resultList);

										return new ResponseEntity<>(storeResponse, HttpStatus.OK);

									} else {
										message = "Update store failed: Changes could not be applied to the store :"
												+ store.getStoreName();
										logger.error(message);
										resultList.add(result);
										storeResponse.setMessage(message);
										storeResponse.setResult(new ArrayList<ResultStore>());
										return new ResponseEntity<>(storeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
									}

								} else {
									message = "Bad Request:Store name could not be blank.";
									logger.error(message);
									storeResponse.setMessage(message);
									storeResponse.setResult(resultList);
									return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
								}

							} else {
								message = "Bad Request: Invalid user role.";
								logger.error(message);
								storeResponse.setMessage(message);
								storeResponse.setResult(resultList);
								return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
							}

						} else {
							message = "Update store failed: Unable to find the user with email :"
									+ store.getCreatedBy();
							logger.error(message);
							storeResponse.setMessage(message);
							storeResponse.setResult(resultList);
							return new ResponseEntity<>(storeResponse, HttpStatus.NOT_FOUND);
						}

					} else {
						message = "Bad Request: Store Update user field could not be blank.";
						logger.error(message);
						storeResponse.setMessage(message);
						storeResponse.setResult(resultList);
						return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
					}

				} else {
					message = "Unable to find the store with id :" + store.getStoreId();
					logger.error(message);
					storeResponse.setMessage(message);
					storeResponse.setResult(resultList);
					return new ResponseEntity<>(storeResponse, HttpStatus.NOT_FOUND);
				}

			} else {
				message = "Bad Request:Store Id could not be blank.";
				logger.error(message);
				storeResponse.setMessage(message);
				storeResponse.setResult(resultList);
				return new ResponseEntity<>(storeResponse, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error, " + e.toString());
			storeResponse.setMessage("Error, " + e.toString());
			storeResponse.setResult(resultList);
			return new ResponseEntity<>(storeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
