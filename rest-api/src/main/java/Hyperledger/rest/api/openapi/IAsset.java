/*
 * SPDX-License-Identifier: Apache-2.0
 */
package Hyperledger.rest.api.openapi;

import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@javax.ws.rs.Path("/asset")
@ApplicationScoped
@OpenAPIDefinition(info = @Info(title = "asset endpoint", version = "1.0"))
public class IAsset {

	// set this for the location of the wallet directory and the connection json
	String currentDir = "";
	Path currentUsersHomeDir = Paths.get(System.getProperty("user.dir"));
	Path pathRoot = Paths.get(currentUsersHomeDir.toString(), "Users", "Shared", "Connections");
	String connectionFile = "//UserOrg1GatewayConnection.json";
	String user = "jiadi";

	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Account for id", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "Asset"))),
			@APIResponse(responseCode = "500", description = "Internal server error.") })

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("get-balance")
	@Operation(summary = "Get balance from the Blockchain", description = "Requires several parameters to be provided")
	public Response getBalance(@QueryParam("id") int id, @QueryParam("timestamp") String timestamp,
			@QueryParam("account") String account, @QueryParam("key") String key,
			@QueryParam("signature") String signature) {
		byte[] result = null;
		try {
			String sigInput = RSAHelper.decryptSplit(signature, key);
			String queryInput = id + timestamp + "account" + account + key;
			String userId = account;

			if (!queryInput.equals(sigInput)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", id);
				jsonObject.put("message", "Request parameters is invalid");
				String errorMsg = jsonObject.toJSONString();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*")
						.entity(errorMsg).build();
			}

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);

			// load a CCP
			// expecting the connect profile json file; export the Connection Profile from
			// the
			// fabric gateway and add to the default server location
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);
			Gateway.Builder builder = Gateway.createBuilder();

			// expecting wallet directory within the default server location
			// wallet exported from Fabric wallets Org 1
			builder.identity(wallet, user).networkConfig(networkConfigPath).discovery(true);

			try (Gateway gateway = builder.connect()) {

				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");

				// query public key
				byte[] pubKey = contract.evaluateTransaction("readPublicKey", userId);
				String scKey = new String(pubKey);

				if (scKey.equals("FROM_USER_NOT_FOUND")) {
					JSONObject jsonObjPK = new JSONObject();
					jsonObjPK.put("id", id);
					jsonObjPK.put("message", "The account " + userId + " does not exsit");
					String errorMsg = jsonObjPK.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.header("Access-Control-Allow-Origin", "*").entity(errorMsg).build();
				} else {
					if (!scKey.equals(key)) {
						JSONObject jsonObjPK = new JSONObject();
						jsonObjPK.put("id", id);
						jsonObjPK.put("message", "Wrong user, you don't have the permission");
						String errorMsg = jsonObjPK.toJSONString();
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
								.header("Access-Control-Allow-Origin", "*").entity(errorMsg).build();
					}
				}

				// query balance
				result = contract.evaluateTransaction("readBalance", userId);
				String resultStr = new String(result);
				JSONObject jsonObject = JSONObject.parseObject(resultStr);
				System.out.println(resultStr);
				System.out.println(jsonObject.get("publicKey") == null);
				if (jsonObject.get("balance") == null) {
					jsonObject.remove("balance");
					jsonObject.put("id", id);
					jsonObject.put("message", "The account " + userId + " does not exsit");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.header("Access-Control-Allow-Origin", "*").entity(errorMsg).build();
				}
				jsonObject.put("id", id);
				String succMsg = jsonObject.toJSONString();
				return Response.status(Response.Status.OK).header("Access-Control-Allow-Origin", "*").entity(succMsg)
						.build();
			} catch (Exception e) {
				System.out.println("Unable to get network/contract and execute query");
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new javax.ws.rs.ServiceUnavailableException();
		}

	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Transfer Money Information", description = "Requires several parameters to be provided")
	@javax.ws.rs.Path("send")
	public Response send(SendRequest sendRequest) {
		try {
			int id = sendRequest.getId();
			String fromId = sendRequest.getFromId();
			String toID = sendRequest.getToId();
			double amount = sendRequest.getAmount();
			String timestamp = sendRequest.getTimestamp();
			String key = sendRequest.getKey();
			String signature = sendRequest.getSignature();
			// @QueryParam("id") int id, @QueryParam("fromId") String fromId,
			// @QueryParam("toID") String toID, @QueryParam("amount") double amount,
			// @QueryParam("timestamp") String timestamp, @QueryParam("key") String key,
			// @QueryParam("signature") String signature
			String sigInput = RSAHelper.decryptSplit(signature, key);
			String queryInput = id + timestamp + "fromId" + fromId + "toId" + toID + "amount" + amount + key;

			if (!queryInput.equals(sigInput)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", id);
				jsonObject.put("message", "Request parameters is invalid");
				String errorMsg = jsonObject.toJSONString();

				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*")
						.entity(errorMsg).build();
			}
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes();

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath)
					.discovery(true);
			try (Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");

				// check public key
				byte[] pubKey = contract.evaluateTransaction("readPublicKey", fromId);
				String scKey = new String(pubKey);

				if (scKey.equals("FROM_USER_NOT_FOUND")) {
					JSONObject jsonObjPK = new JSONObject();
					jsonObjPK.put("id", id);
					jsonObjPK.put("message", "The account " + fromId + " does not exsit");
					String errorMsg = jsonObjPK.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.header("Access-Control-Allow-Origin", "*").entity(errorMsg).build();
				} else {
					if (!scKey.equals(key)) {
						JSONObject jsonObjPK = new JSONObject();
						jsonObjPK.put("id", id);
						jsonObjPK.put("message", "Wrong user, you don't have the permission");
						String errorMsg = jsonObjPK.toJSONString();
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
								.header("Access-Control-Allow-Origin", "*").entity(errorMsg).build();
					}
				}

				// send request
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("transferMoney");
				response = transaction.submit(fromId, toID, String.valueOf(amount));
				String responseMsg = new String(response);
				System.out.println(responseMsg);
				System.out.println(responseMsg.equals("FROM_USER_NOT_FOUND"));
				if (responseMsg.equals("FROM_USER_NOT_FOUND")) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", id);
					jsonObject.put("message", "The account " + fromId + " does not exsit");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
				} else if (responseMsg.equals("TO_USER_NOT_FOUND")) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", id);
					jsonObject.put("message", "The account " + toID + " does not exsit");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
				} else if (responseMsg.equals("MONEY_NOT_ENOUGH")) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", id);
					jsonObject.put("message", "The account " + fromId + " does not have enough money");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", id);
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.OK).entity(errorMsg).build();
				}

			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString()
						+ ", Response : " + e.getProposalResponses().toString());
				return Response.status(Response.Status.BAD_REQUEST).entity(new String(response)).build();
				// throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.toString());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("userID")
	@Operation(summary = "Check if Account Exist in the Blockchain", description = "Requires the key to be provided")
	public Response assetExists(@QueryParam("userID") String userId) {
		byte[] result = null;

		try {
			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);

			// load a CCP
			// expecting the connect profile json file; export the Connection Profile from
			// the
			// fabric gateway and add to the default server location
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);
			Gateway.Builder builder = Gateway.createBuilder();

			// expecting wallet directory within the default server location
			// wallet exported from Fabric wallets Org 1
			builder.identity(wallet, user).networkConfig(networkConfigPath).discovery(true);

			try (Gateway gateway = builder.connect()) {

				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				result = contract.evaluateTransaction("assetExists", userId);
				return Response.status(Response.Status.OK).entity(new String(result)).build();
			} catch (Exception e) {
				System.out.println("Unable to get network/contract and execute query");
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Add a new Tag to the ledger", description = "Requires a unique key starting with TAG to be successfull")
	@javax.ws.rs.Path("createAsset")
	public Response createAsset(AssetRequest createAssetRequest) {
		try {
			String userId = createAssetRequest.getUserId();
			Double balance = createAssetRequest.getBalance();
			String publicKey = createAssetRequest.getPublicKey();
			// @QueryParam("userID") String userId, @QueryParam("balance") double balance,
			// @QueryParam("publicKey") String publicKey
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes();

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath)
					.discovery(true);
			try (Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("createAsset");
				response = transaction.submit(userId, String.valueOf(balance), publicKey);
				String responseMsg = new String(response);
				if (responseMsg.equals("USER_ALREADY_EXISTS")) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("message", "The account " + userId + " is already exsit");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("message", "Success");
					String succMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.OK).entity(succMsg).build();
				}

			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString()
						+ ", Response : " + e.getProposalResponses().toString());
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.toString());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("readAsset")
	@Operation(summary = "Get all asset information from the Blockchain", description = "Requires the key to be provided")
	public Response readAsset(@QueryParam("userId") String userId) {
		byte[] result = null;

		try {
			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);

			// load a CCP
			// expecting the connect profile json file; export the Connection Profile from
			// the
			// fabric gateway and add to the default server location
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);
			Gateway.Builder builder = Gateway.createBuilder();

			// expecting wallet directory within the default server location
			// wallet exported from Fabric wallets Org 1
			builder.identity(wallet, user).networkConfig(networkConfigPath).discovery(true);

			try (Gateway gateway = builder.connect()) {

				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				result = contract.evaluateTransaction("readAsset", userId);
				String resultStr = new String(result);
				JSONObject jsonObject = JSONObject.parseObject(resultStr);

				if (jsonObject.get("publicKey") == null) {
					jsonObject.put("message", "The account " + userId + " does not exsit");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.header("Access-Control-Allow-Origin", "*").entity(errorMsg).build();
				}
				return Response.status(Response.Status.OK).entity(new String(result)).build();
			} catch (Exception e) {
				System.out.println("Unable to get network/contract and execute query");
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@javax.ws.rs.Path("get-all")
	@Operation(summary = "Get all asset information from the Blockchain", description = "Require nothing")
	public Response getAll() {
		byte[] result = null;

		try {
			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);

			// load a CCP
			// expecting the connect profile json file; export the Connection Profile from
			// the
			// fabric gateway and add to the default server location
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);
			Gateway.Builder builder = Gateway.createBuilder();

			// expecting wallet directory within the default server location
			// wallet exported from Fabric wallets Org 1
			builder.identity(wallet, user).networkConfig(networkConfigPath).discovery(true);

			try (Gateway gateway = builder.connect()) {

				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				result = contract.evaluateTransaction("getAllUsers");
				return Response.status(Response.Status.OK).entity(new String(result)).build();
			} catch (Exception e) {
				System.out.println("Unable to get network/contract and execute query");
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update Balance", description = "Requires the Account ID and the New Value to be provided")
	@javax.ws.rs.Path("updateBalance")
	public Response updateBalance(AssetRequest assetRequest) {
		try {
			// @QueryParam("userId") String userId, @QueryParam("newValue") double newValue
			String userId = assetRequest.getUserId();
			double newValue = assetRequest.getNewValue();
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes();

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath)
					.discovery(true);
			try (Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("updateBalance");
				response = transaction.submit(userId, String.valueOf(newValue));
				String responseMsg = new String(response);
				if (responseMsg.equals("FROM_USER_NOT_FOUND")) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("message", "The account " + userId + " does not exsit");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
				} else {
					return Response.status(Response.Status.OK).entity(responseMsg).build();
				}
			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString()
						+ ", Response : " + e.getProposalResponses().toString());
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.toString());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Delete Account", description = "Requires the Account ID be provided")
	@javax.ws.rs.Path("updateAsset")
	public Response deleteAsset(AssetRequest assetRequest) {
		try {
			String userId = assetRequest.getUserId();
			// @QueryParam("userId") String userId
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes();

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath)
					.discovery(true);
			try (Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("deleteAsset");
				response = transaction.submit(userId);
				String responseMsg = new String(response);
				if (responseMsg.equals("FROM_USER_NOT_FOUND")) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("message", "The account " + userId + " does not exsit");
					String errorMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("message", "Success");
					String succMsg = jsonObject.toJSONString();
					return Response.status(Response.Status.OK).entity(succMsg).build();
				}
			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString()
						+ ", Response : " + e.getProposalResponses().toString());

				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		} catch (Exception e) {
			System.out.println(e.toString());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
	}
}
