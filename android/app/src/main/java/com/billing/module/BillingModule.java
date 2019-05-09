package com.billing.module;

import android.support.annotation.Nullable;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.List;
import javax.annotation.Nonnull;

public class BillingModule extends ReactContextBaseJavaModule implements PurchasesUpdatedListener {
  private final String LOG_TAG = BillingModule.class.getSimpleName();
  private BillingClient billingClient;
  private boolean isServiceConnected;
  private ReactApplicationContext context;

  public BillingModule(@Nonnull ReactApplicationContext reactContext) {
    super(reactContext);
    context = reactContext;
    //openConnection(null, reactContext);
    billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
  }

  @Nonnull @Override public String getName() {
    return "BillingApiLib";
  }

  @Override
  public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

  }

  @ReactMethod
  public void queryPurchaseHistoryAsync(String SKUType, final Promise promise) {
    Runnable historyRunnable = () -> {
      if (billingClient != null && billingClient.isReady()) {
        billingClient.queryPurchaseHistoryAsync(SKUType, (billingResult, purchaseHistoryRecordList) -> {
          if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (promise != null) {
              promise.resolve(purchaseHistoryRecordList);
            }
          } else {
            if (promise != null) {
              promise.reject(new Throwable());
            }
          }
        });
      }
    };

    executeServiceRequest(historyRunnable);
  }

  private void executeServiceRequest(Runnable runnable) {
    if (isServiceConnected) {
      runnable.run();
    } else {
      startServiceConnection(runnable, null);
    }
  }

  private void startServiceConnection(final Runnable executeOnSuccess, final Promise promise) {
    billingClient.startConnection(new BillingClientStateListener() {
      @Override public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
          isServiceConnected = true;

          if (executeOnSuccess != null) {
            executeOnSuccess.run();
          }
        }

        if (promise != null) {
          promise.resolve(billingResult);
        }
      }

      @Override public void onBillingServiceDisconnected() {
        isServiceConnected = false;

        if (promise != null) {
          promise.resolve(false);
        }
      }
    });
  }

  // for test
  @ReactMethod
  public void isReady(final Promise promise) {
    promise.resolve(billingClient != null ? "Yes" : "No");
  }

  @ReactMethod
  public void openConnection(final Promise promise) {
    startServiceConnection(null, promise);
  }

  @ReactMethod
  public void closeConnection(final Promise promise) {
    if (billingClient != null && billingClient.isReady()) {
      billingClient.endConnection();
      billingClient = null;

      if (promise != null) {
        promise.resolve(true);
      }
    }
  }
}
