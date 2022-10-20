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
	String user = "sachi";
    
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Asset for id", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "Asset"))),
            @APIResponse(responseCode = "404", description = "No Asset found for the id.") })

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @javax.ws.rs.Path("userID")
	@Operation(summary = "Check if Asset Exist in the Blockchain", description = "Requires the key to be provided")
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
		} 
		catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		}
		catch (Exception e)		
		{
			System.out.println(e.getMessage());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
    }

    @POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Add a new Tag to the ledger", description = "Requires a unique key starting with TAG to be successfull")
	@javax.ws.rs.Path("createAsset")
    public Response createAsset(@QueryParam("userID")String userId,@QueryParam("balance") String balance,@QueryParam("publicKey")String publicKey) {
        try {
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes() ;

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath).discovery(true);
			try(Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("createAsset");
				response = transaction.submit(userId,balance,publicKey);
				return Response.status(Response.Status.OK).entity(new String(response)).build();
			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString() + ", Response : " + e.getProposalResponses().toString() );
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		}
		catch (Exception e)		
		{
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
				return Response.status(Response.Status.OK).entity(new String(result)).build();
			} catch (Exception e) {
				System.out.println("Unable to get network/contract and execute query");
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		}
		catch (Exception e)		
		{
			System.out.println(e.getMessage());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
    }

	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @javax.ws.rs.Path("get-balance")
	@Operation(summary = "Get all asset information from the Blockchain", description = "Requires the key to be provided")
    public Response getBalance(@QueryParam("id") String id,@QueryParam("timestamp") String timestamp,
		@QueryParam("account") String account,@QueryParam("key") String key,@QueryParam("signature") String signature) {
		byte[] result = null;
		try{
			// String input = Decode.encrypt(signature, key);
			// JSONObject jsonObject = JSONObject.parseObject(input);
			// String sigId = jsonObject.getString("id");
			// String sigTime = jsonObject.getString("timestamp");
			// String sigUser = jsonObject.getJSONObject("params").getString("account");
			// String sigKey = jsonObject.getString("key");

			// JSONObject jsonObjParam = JSONObject.parseObject(params);
			// String userId = jsonObjParam.getString("account");
			String userId = account;

			boolean isSame = false;
			// if (sigId != id || sigTime != timestamp || sigUser != userId || sigKey != key) {
			// 	isSame = false;
			// }
			if (!isSame) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", id);
				jsonObject.put("message", "Request parameters is invalid");
				String errorMsg = jsonObject.toJSONString();
				// String errorMsg = "{\"id\":\"3\",\"message\":\"Request parameters is invalid\"}";
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*").entity(errorMsg).build();
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
					result = contract.evaluateTransaction("readBalance", userId);
					String resultStr = new String(result);
					System.out.println(resultStr);
					return Response.status(Response.Status.OK).entity(new String(result)).build();
				} catch (Exception e) {
					System.out.println("Unable to get network/contract and execute query");
					throw new javax.ws.rs.ServiceUnavailableException();
				}
			} 
			catch (IOException e) {
				System.out.println("Current working dir: " + currentDir);
				System.out.println(
						"Unable to find config or wallet - please check the wallet directory and connection json");
				throw new javax.ws.rs.NotFoundException();
			}
			catch (Exception e)		
			{
				System.out.println(e.getMessage());
				throw new javax.ws.rs.ServiceUnavailableException();
			}

    }

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update Asset Information", description = "Requires the Asset ID and the New Value to be provided")
	@javax.ws.rs.Path("updateBalance")
    public Response updateBalance(@QueryParam("userId") String userId,@QueryParam("newValue") String newValue) {
        try {
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes() ;

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath).discovery(true);
			try(Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("updateBalance");
				
				response = transaction.submit(userId, newValue);
				System.out.println("line 206"+new String(response));
				String responseMsg = new String(response);
				if (responseMsg == "") {
					return Response.status(Response.Status.OK).entity("Error: User does not exist" + userId).build();
				}else {
					return Response.status(Response.Status.BAD_REQUEST).entity(responseMsg + userId).build();
				}
			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString() + ", Response : " + e.getProposalResponses().toString() );
				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		}
		catch (Exception e)		
		{
			System.out.println(e.toString());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
    }

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Transfer Asset Information", description = "Requires the Asset ID and the New Value to be provided")
	@javax.ws.rs.Path("send")
    public Response send(@QueryParam("fromId") String fromId,@QueryParam("toID") String toID,@QueryParam("amount")String amount) {
        try {
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes() ;

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath).discovery(true);
			try(Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("transferMoney");
				response = transaction.submit(fromId,toID,amount);
				return Response.status(Response.Status.OK).entity(new String(response)).build();
			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString() + ", Response : " + e.getProposalResponses().toString() );
				return Response.status(Response.Status.BAD_REQUEST).entity(new String(response)).build();
				// throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		}
		catch (Exception e)		
		{
			System.out.println(e.toString());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
    }
    
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update Asset Information", description = "Requires the Asset ID and the New Value to be provided")
	@javax.ws.rs.Path("updateAsset")
    public Response deleteAsset(@QueryParam("userId") String userId) {
        try {
			currentDir = new java.io.File(".").getCanonicalPath();
			byte[] response = new String("Null").getBytes() ;

			Path walletPath = Paths.get(pathRoot.toString(), "wallet");
			Wallet wallet = Wallets.newFileSystemWallet(walletPath);
			Path networkConfigPath = Paths.get(pathRoot + connectionFile);

			Gateway.Builder builder = Gateway.createBuilder().identity(wallet, user).networkConfig(networkConfigPath).discovery(true);
			try(Gateway gateway = builder.connect()) {
				// get the network and contract
				Network network = gateway.getNetwork("mychannel");
				org.hyperledger.fabric.gateway.Contract contract = network.getContract("asset");
				org.hyperledger.fabric.gateway.Transaction transaction = contract.createTransaction("deleteAsset");
				response = transaction.submit(userId);
				return Response.status(Response.Status.OK).entity(new String(response)).build();
			} catch (ContractException e) {
				System.out.println("Unable to get network/contract and execute query : " + e.toString() + ", Response : " + e.getProposalResponses().toString() );

				throw new javax.ws.rs.ServiceUnavailableException();
			}
		} 
		catch (IOException e) {
			System.out.println("Current working dir: " + currentDir);
			System.out.println(
					"Unable to find config or wallet - please check the wallet directory and connection json");
			throw new javax.ws.rs.NotFoundException();
		}
		catch (Exception e)		
		{
			System.out.println(e.toString());
			throw new javax.ws.rs.ServiceUnavailableException();
		}
    }
}
