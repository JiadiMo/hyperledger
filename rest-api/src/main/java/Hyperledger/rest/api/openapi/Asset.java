/*
 * SPDX-License-Identifier: Apache-2.0
 */

package Hyperledger.rest.api.openapi;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.owlike.genson.Genson;

@Schema(name="Asset")
public class Asset {

    private final static Genson genson = new Genson();

    private double balance;

    private String publicKey;

    public Asset(){
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    public static Asset fromJSONString(String json) {
        Asset asset = genson.deserialize(json, Asset.class);
        return asset;
    }
}
