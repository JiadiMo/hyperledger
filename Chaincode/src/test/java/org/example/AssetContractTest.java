/*
 * SPDX-License-Identifier: Apache License 2.0
 */

package org.example;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


public final class AssetContractTest {
    private enum TransStatus {
        FROM_USER_NOT_FOUND,
        TO_USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        MONEY_NOT_ENOUGH,
        SUCCESS,
    }

    @Nested
    class AssetExists {
        @Test
        public void noProperAsset() {

            AssetContract contract = new  AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(new byte[] {});
            boolean result = contract.assetExists(ctx,"10001");
            assertFalse(result);
        }

        @Test
        public void assetExists() {

            AssetContract contract = new  AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(new byte[] {42});
            boolean result = contract.assetExists(ctx,"10001");

            assertTrue(result);

        }

        @Test
        public void noKey() {
            AssetContract contract = new  AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10002")).thenReturn(null);
            boolean result = contract.assetExists(ctx,"10002");

            assertFalse(result);

        }

    }

    @Nested
    class AssetCreates {

        @Test
        public void newAssetCreate() {
            AssetContract contract = new  AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            String json = "{\"balance\":100.0,\"publicKey\":\"key1\"}";

            contract.createAsset(ctx, "10001", 100, "key1" );

            verify(stub).putState("10001", json.getBytes(UTF_8));
        }

        @Test
        public void alreadyExists() {
            AssetContract contract = new  AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10002")).thenReturn(new byte[] { 42 });

            String res = contract.createAsset(ctx, "10002", 200, "key2");

            assertEquals(res, TransStatus.USER_ALREADY_EXISTS.toString());

        }

    }

    @Test
    public void assetRead() {
        AssetContract contract = new  AssetContract();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        Asset asset = new  Asset();
        asset.setBalance(500.0);

        String json = asset.toJSONString();
        when(stub.getState("10001")).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        Asset returnedAsset = contract.readAsset(ctx, "10001");
        assertEquals(returnedAsset.getBalance(), asset.getBalance());
    }

    @Nested
    class AssetUpdates {
        @Test
        public void updateExisting() {
            AssetContract contract = new  AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getState("10001")).thenReturn(new byte[] { 42 });

            contract.updateBalance(ctx, "10001", 800.0);

            String json = "{\"balance\":800.0,\"publicKey\":\"key1\"}";
            verify(stub).putState("10001", json.getBytes(UTF_8));
        }

        @Test
        public void updateMissing() {
            AssetContract contract = new  AssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(null);

            String res = contract.updateBalance(ctx, "10001", 350.0);

            assertEquals(res, TransStatus.FROM_USER_NOT_FOUND.toString());
        }

    }

    @Test
    public void assetDelete() {
        AssetContract contract = new  AssetContract();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getState("10001")).thenReturn(null);

        String res = contract.deleteAsset(ctx, "10001");

        assertEquals(res, TransStatus.FROM_USER_NOT_FOUND.toString());
    }

}
