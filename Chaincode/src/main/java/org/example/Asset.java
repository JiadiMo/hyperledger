/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;

@DataType()
public class Asset {

    private final static Genson genson = new Genson();

    @Property()
    private double balance;

    @Property()
    private String publicKey;

    public Asset(double balance, String publicKey) {
        this.balance = balance;
        this.publicKey = publicKey;
    }

    public Asset() {

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

    @Override
    public int hashCode() {
        return Objects.hash(getBalance(), getPublicKey());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [balance=" + balance
                + ", publicKey=" + publicKey + "]";
    }
}
