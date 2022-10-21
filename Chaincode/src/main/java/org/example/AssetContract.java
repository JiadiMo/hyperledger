/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(name = "AssetContract", info = @Info(title = "Asset contract", description = "My Smart Contract", version = "0.0.1", license = @License(name = "Apache-2.0", url = ""), contact = @Contact(email = "HyperledgerFab_UWB_Chaincode@example.com", name = "HyperledgerFab_UWB_Chaincode", url = "http://HyperledgerFab_UWB_Chaincode.me")))
@Default
public class AssetContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum TransStatus {
        FROM_USER_NOT_FOUND,
        TO_USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        MONEY_NOT_ENOUGH,
        SUCCESS,
    }

    public AssetContract() {

    }

    @Transaction()
    public Asset readBalance(Context ctx, String userID) {
        boolean exists = assetExists(ctx, userID);
        if (!exists) {
            // throw new RuntimeException("The account "+userID+" does not exist");
            Asset resAsset = new Asset();
            return resAsset;
        } else {
            Asset newAsset = Asset.fromJSONString(new String(ctx.getStub().getState(userID), UTF_8));
            Asset resAsset = new Asset();
            resAsset.setBalance(newAsset.getBalance());
            return resAsset;
        }
    }

    @Transaction()
    public String readPublicKey(Context ctx, String userID) {
        boolean exists = assetExists(ctx, userID);
        if (!exists) {
            // throw new RuntimeException("The account "+userID+" does not exist");
            return TransStatus.FROM_USER_NOT_FOUND.toString();
        } else {
            Asset newAsset = Asset.fromJSONString(new String(ctx.getStub().getState(userID), UTF_8));
            return newAsset.getPublicKey();
        }
    }

    @Transaction()
    public String transferMoney(Context ctx, String fromID, String toID, double amount) {
        boolean existsFromID = assetExists(ctx, fromID);
        // Check account exist
        if (!existsFromID) {
            // throw new RuntimeException("The account " + fromID + " does not exist");
            return TransStatus.FROM_USER_NOT_FOUND.toString();
        }
        boolean existsToID = assetExists(ctx, toID);
        if (!existsToID) {
            // throw new RuntimeException("The account " + toID + " does not exist");
            return TransStatus.TO_USER_NOT_FOUND.toString();
        }

        // Check balance
        Asset fromAsset = Asset.fromJSONString(new String(ctx.getStub().getState(fromID), UTF_8));
        if (fromAsset.getBalance() < amount) {
            // throw new RuntimeException("Balance of account " + fromID + " is not
            // enough");
            return TransStatus.MONEY_NOT_ENOUGH.toString();
        }
        Asset toAsset = Asset.fromJSONString(new String(ctx.getStub().getState(toID), UTF_8));

        fromAsset.setBalance(fromAsset.getBalance() - amount);
        toAsset.setBalance(toAsset.getBalance() + amount);

        ctx.getStub().putState(fromID, fromAsset.toJSONString().getBytes(UTF_8));
        ctx.getStub().putState(toID, toAsset.toJSONString().getBytes(UTF_8));

        return TransStatus.SUCCESS.toString();
    }

    @Transaction()
    public boolean assetExists(Context ctx, String userID) {
        byte[] buffer = ctx.getStub().getState(userID);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public String createAsset(Context ctx, String userID, double balance, String publicKey) {
        boolean exists = assetExists(ctx, userID);
        if (exists) {
            // throw new RuntimeException("The asset "+userID+" already exists");
            return TransStatus.USER_ALREADY_EXISTS.toString();
        }
        Asset asset = new Asset(balance, publicKey);
        ctx.getStub().putState(userID, asset.toJSONString().getBytes(UTF_8));
        return TransStatus.SUCCESS.toString();
    }

    @Transaction()
    public Asset readAsset(Context ctx, String userID) {
        boolean exists = assetExists(ctx, userID);
        if (!exists) {
            // throw new RuntimeException("The account "+userID+" does not exist");
            Asset resAsset = new Asset();
            return resAsset;
        }
        Asset newAsset = Asset.fromJSONString(new String(ctx.getStub().getState(userID), UTF_8));
        return newAsset;
    }

    @Transaction()
    public String getAllUsers(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Asset> queryResults = new ArrayList<Asset>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result : results) {
            Asset asset = Asset.fromJSONString(new String(result.getValue(), UTF_8));
            queryResults.add(asset);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    @Transaction()
    public String updateBalance(Context ctx, String userID, double newValue) {
        boolean exists = assetExists(ctx, userID);
        if (!exists) {
            // throw new RuntimeException("The asset "+userID+" does not exist");
            return TransStatus.FROM_USER_NOT_FOUND.toString();
        }
        Asset asset = Asset.fromJSONString(new String(ctx.getStub().getState(userID), UTF_8));
        asset.setBalance(newValue);

        ctx.getStub().putState(userID, asset.toJSONString().getBytes(UTF_8));
        return userID;
    }

    @Transaction()
    public String updatePublicKey(Context ctx, String userID, String publicKey) {
        boolean exists = assetExists(ctx, userID);
        if (!exists) {
            // throw new RuntimeException("The asset "+userID+" does not exist");
            return TransStatus.FROM_USER_NOT_FOUND.toString();
        }
        Asset asset = new Asset();
        asset.setPublicKey(publicKey);

        ctx.getStub().putState(userID, asset.toJSONString().getBytes(UTF_8));
        return TransStatus.SUCCESS.toString();
    }

    @Transaction()
    public String deleteAsset(Context ctx, String userId) {
        boolean exists = assetExists(ctx, userId);
        if (!exists) {
            // throw new RuntimeException("The asset "+userId+" does not exist");
            return TransStatus.FROM_USER_NOT_FOUND.toString();
        }
        ctx.getStub().delState(userId);
        return TransStatus.SUCCESS.toString();
    }

}
