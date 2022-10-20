/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.shim.ChaincodeException;
import static java.nio.charset.StandardCharsets.UTF_8;

@Contract(name = "AssetContract",
    info = @Info(title = "Asset contract",
                description = "My Smart Contract",
                version = "0.0.1",
                license =
                        @License(name = "Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "HyperledgerFab_UWB_Chaincode@example.com",
                                                name = "HyperledgerFab_UWB_Chaincode",
                                                url = "http://HyperledgerFab_UWB_Chaincode.me")))
@Default
public class AssetContract implements ContractInterface {
    public  AssetContract() {

    }
    @Transaction()
    public boolean assetExists(Context ctx, String userID) {
        byte[] buffer = ctx.getStub().getState(userID);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public Asset createAsset(Context ctx, String userID, double balance, String publicKey) {
        boolean exists = assetExists(ctx,userID);
        if (exists) {
            // throw new RuntimeException("The asset "+userID+" already exists");
            Asset asset = new Asset();
            return asset;
        }
        else{
            Asset asset = new Asset(userID, balance, publicKey);
            ctx.getStub().putState(userID, asset.toJSONString().getBytes(UTF_8));
            return asset;
        }
    }

    @Transaction()
    public Asset readAsset(Context ctx, String userID) {
        boolean exists = assetExists(ctx,userID);
        if (!exists) {
            // throw new RuntimeException("The account "+userID+" does not exist");
            
        }

        Asset newAsset = Asset.fromJSONString(new String(ctx.getStub().getState(userID),UTF_8));
        return newAsset;
    }

    @Transaction()
    public Asset readBalance(Context ctx, String userID) {
        boolean exists = assetExists(ctx,userID);
        if (!exists) {
            // throw new RuntimeException("The account "+userID+" does not exist");
            Asset resAsset = new Asset();
            return resAsset;
        }
        else{
            Asset newAsset = Asset.fromJSONString(new String(ctx.getStub().getState(userID),UTF_8));
            Asset resAsset = new Asset();
            resAsset.setBalance(newAsset.getBalance());
            return resAsset;
        }
    }

    @Transaction()
    public String transferMoney(Context ctx, String fromID, String toID, double amount) {
        boolean existsFromID = assetExists(ctx, fromID);
        if (!existsFromID) {
            // throw new RuntimeException("The account " + fromID + " does not exist");
            String errorMessage = "The account " + fromID + " does not exist";
            return errorMessage;
        }
        boolean existsToID = assetExists(ctx, toID);
        if (!existsToID) {
            // throw new RuntimeException("The account " + toID + " does not exist");
            String errorMessage = "The account " + toID + " does not exist";
            return errorMessage;
        }

        // Check balance
        Asset fromAsset = Asset.fromJSONString(new String(ctx.getStub().getState(fromID),UTF_8));
        if (fromAsset.getBalance() < amount) {
            // throw new RuntimeException("Balance of asset " + fromID + " is not enough");
            String errorMessage = "The account " + toID + " does not exist";
            return errorMessage;
        }
        Asset toAsset = Asset.fromJSONString(new String(ctx.getStub().getState(toID),UTF_8));
        
        fromAsset.setBalance(fromAsset.getBalance() - amount);
        toAsset.setBalance(toAsset.getBalance() + amount);

        ctx.getStub().putState(fromID, fromAsset.toJSONString().getBytes(UTF_8));
        ctx.getStub().putState(toID, toAsset.toJSONString().getBytes(UTF_8));

        // Asset newAsset = Asset.fromJSONString(new String(ctx.getStub().getState(toID),UTF_8));
        String successMessage = "Success";
        return successMessage;
    }

    @Transaction()
    public String updateBalance(Context ctx, String userID, double newValue) {
        boolean exists = assetExists(ctx,userID);
        if (!exists) {
            String errorMessage = String.format("Error: User %s does not exist", userID);
            System.out.println(errorMessage);
            return "";
            // throw new RuntimeException("The asset "+userID+" does not exist");
        }
        Asset asset = Asset.fromJSONString(new String(ctx.getStub().getState(userID),UTF_8));
        asset.setBalance(newValue);

        ctx.getStub().putState(userID, asset.toJSONString().getBytes(UTF_8));
        return userID;
    }

    @Transaction()
    public void updatePublicKey(Context ctx, String userID, String publicKey) {
        boolean exists = assetExists(ctx,userID);
        if (!exists) {
            throw new RuntimeException("The asset "+userID+" does not exist");
        }
        Asset asset = new Asset();
        asset.setPublicKey(publicKey);

        ctx.getStub().putState(userID, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public void deleteAsset(Context ctx, String userId) {
        boolean exists = assetExists(ctx,userId);
        if (!exists) {
            throw new RuntimeException("The asset "+userId+" does not exist");
        }
        ctx.getStub().delState(userId);
    }

}
