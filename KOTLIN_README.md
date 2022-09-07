![Screenshot](docs/clearent_logo.jpg)

# Clearent SDK UI

## Overview

Clearent SDK UI is a wrapper over ClearentFrameworkSDK that provides payment capabilities using the IDTech iOS framework to read credit card data using VP3300. Its goal is to ease integration by providing complete UI that handle all important flows end-to-end.


 **Clearent SDK UI** wraps all major features of the ClearentFrameworkSDK and adds UI for all major flows:

1. **Pairing Flow**, guides the user through the pairing process steps, taking care of edge cases and possible errors.

2. **Transaction Flow**, guides the user through the transaction flow, handling also device pairing if needed, takes care of edge cases and error handling.

3. **Readers List & Reader Details**, this flow provides reader management capabilities, it displays the status of the current paired reader, but also a list of recently used readers from where you can navigate to a settings screen of the reader.

**Clearent SDK UI - Options**

1. **Tips**, when this feature is enabled the first step in the Transaction Flow will be the tips screen where the user/client is prompted with UI that will offer some options to choose a tip. This feature can be enabled or disabled from your merchant account.

2. **Signature**, when this feature is enabled as a last step in the Transaction Flow the SDK will display a screen  where the user/client can provide a signature. This signature will be uploaded to the Clearent backend.

3. **UI Customization**, Clearent SDK UI provides the integrator the chance to customize the fonts, colors and texts used in the UI, This is achieved by overwriting the public properties of each UI element that is exposed.


## Dependencies  - TODO

 **Clearent SDK UI** does not use any other dependencies except the ones of the ClearentFrameworkSDK:

 - IDTech.xcframework
 - DTech.bundle (responsible for translating error codes to messages)
 - CocoaLumberJack.xcframework


## Package Management - (To be updated with correct information)

// podfile & cartfile example

## Supported Android versions

The SDK supports current version of Android and two previous versions. Currently sdk versions 29, 30 and 31. With unofficial support for versions 21 throughout 29.

## How to Integrate

In order to integrate the **SDK UI** you will need the **API URL**, **API KEY** and the **PUBLIC KEY**.
Use the ClearentWrapper class to update the SDK with this information like this.

```
ClearentWrapper.initializeReader(
    context: Context, // where context is the ApplicationContext
    baseUrl: String,
    publicKey: String,
    apiKey: String
)
```

We recommend calling this method as soon as possible, preferably inside the Application class of your project.

### Important!

**The safe keeping of the **API URL**, **API KEY** and the **PUBLIC KEY** is the integrators responsibility. The SDK stores this information only in memory!**

ClearentSDKUi is an Activity thus it will have to be called through androids activity launcher:

```
val activityLauncher = registerForActivityResult(
	ActivityResultContracts.StartActivityForResult()
) { 
    // Code to check the result of the activity 
}
val intent = Intent(requireContext(), ClearentSDKUi::class.java) // Use this intent to start the activity
activityLauncher.launch(intent)
```

Inside the intent you will be able to pass in the options. 


**Tips**

This feature can be enabled from your merchant account and when it's enabled the first step in the transaction flow will be a prompt where the user/client is prompted with UI that will offer some options to choose a tip. The options the user/client has are three fixed options in percents and a custom tip input field. The three options are customizable by settting the **tipAmounts** that is an array of Int values property of the **ClearentWrapper** as below.

```
ClearentWrapper.setTipPercentages(5, 15, 20)
```


**Disabling the signature functionality**
The signature feature is enabled by default, if you want to disable it you will have to pass the option into the aforementioned intent before launching the activity:
```
intent.putExtra(
    ClearentSDKUi.SDK_WRAPPER_SHOW_SIGNATURE, 
    false
)
```

Now you are ready to use the SDK UI.
In order to display the UI from the SDK you need to pass in what flow you want to be using. If you want to start an action flow you need to set up the following option inside the intent you will start the activity with:

**Starting the pairing process**

```
intent.putExtra(
	ClearentSDKUi.SDK_WRAPPER_ACTION_KEY,
	ClearentSDKUi.SDK_WRAPPER_ACTION_PAIR
)

// If you want to show hints for the pairing process you should also do this:
intent.putExtra(
    ClearentSDKUi.SDK_WRAPPER_SHOW_HINTS,
    true
)
```

**Starting a transaction**

Every time you start a transaction you need to pass the amount as a Double to the intent.
The SDK UI provides the option to enter the card details manually or by using the card reader.

```
intent.putExtra(
	ClearentSDKUi.SDK_WRAPPER_PAYMENT_METHOD,
    PaymentMethod as Parcelable // Where PaymentMethod is an enum inside "com.clearent.idtech.android.wrapper.ui.PaymentMethod" with the values "CARD_READER" and "MANUAL_ENTRY"
)
```

```
intent.putExtra(
	ClearentSDKUi.SDK_WRAPPER_ACTION_KEY,
	ClearentSDKUi.SDK_WRAPPER_ACTION_TRANSACTION
)
intent.putExtra(
	ClearentSDKUi.SDK_WRAPPER_AMOUNT_KEY,
	amount  // The amount of the transaction which is a Double
)
```

**Showing readers list & reader details**

```
intent.putExtra(
	ClearentSDKUi.SDK_WRAPPER_ACTION_KEY,
	ClearentSDKUi.SDK_WRAPPER_ACTION_DEVICES
)
```
The reader details will display the status of the current reader and a list of recently paired readers. From this list the user can navigate to the readers details.


**Reader Status**

If you want to display the reader's status in your app you can implement the  **ReaderStatusListener** interface from **com.clearent.idtech.android.wrapper.model.ReaderStatus**, and then use the addReaderStatusListener(listener: ReaderStatusListener) to register for notifications and respectively removeReaderStatusListener(listener: ReaderStatusListener) to unregister.

The interface contains a method that will be called to notify you of the new ReaderStatus which contains reader related information.
```
fun onReaderStatusUpdate(readerStatus: ReaderStatus?)

```

How to use it.

```
// First we create the object that implements the interface
readerStatusListener = object : ReaderStatusListener {
        override fun onReaderStatusUpdate(readerStatus: ReaderStatus?) {
        // Update your UI
	}
}

// And then we register for the callback inside the OnViewCreated
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
	// your code...
	ClearentWrapper.addReaderStatusListener(readerStatusListener)
	// your code...
}

// When we don't use it anymore we unregister from the callback
override fun onDestroyView() {
	// your code...
    ClearentWrapper.removeReaderStatusListener(readerStatusListener)
	// your code...
}
```

If you want to refresh the ReaderStatus you can always call the following method:

```
ClearentWrapper.startDeviceInfoUpdate()
```


## Customizing the SDK experience

The SDK provides the option to customize the fonts, colors and texts used in the SDK. This can be achieved by overriding certain attributes in styles, strings and colors xml files. Check our [Example](https://) for full customization example.

**Colors**
```
    <color name="color_primary">#FFFF00</color>
    <color name="color_secondary">#00FFFF</color>
```


**Fonts**

You will need to override the fontFamily attribute of the styles "FontFamilyRegular" and "FontFamilyBold".

```
    <style name="FontFamilyRegular" parent="TextAppearance.AppCompat">
        <item name="fontFamily">@font/sf_pro_text_regular</item>
    </style>

    <style name="FontFamilyBold" parent="TextAppearance.AppCompat">
        <item name="fontFamily">@font/sf_pro_text_bold</item>
    </style>
```

**Texts**

In order to customize texts used in the SDK you will need to override strings inside the strings.xml with the corresponding names.

```
    <string name="tips_title">Would you like to add a tip?</string>
    <string name="tips_confirmation_button_text">Charge $%1$s</string>
    <string name="tips_skip_button_text">Maybe next time</string>
    <string name="tips_percentage_label">"%1$d%% ($%2$s)"</string>
```


## Code Example

Kotlin example of the ClearentSDKUI integration [Java Example](https://github.com/clearent/ClearentSDKUIDemo/tree/Java).


```
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initClearentWrapper()
    }

    private fun initClearentWrapper() = ClearentWrapper.initializeReader(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            Constants.PUBLIC_KEY_SANDBOX,
            Constants.API_KEY_SANDBOX
        )
}
```

```
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This method will setup the ClearentDataSource object to send data over 
        // to the ClearentSDKUi. You can also implement your custom *ClearentWrapperListener*.
        ClearentWrapper.setListener(ClearentDataSource)
    }

    override fun onDestroy() {
        super.onDestroy()
        ClearentWrapper.removeListener()
    }
}
```



# Integrating the ClearentWrapper


## Overview

**ClearentWrapper** is a wrapper over **ClearentFrameworkSDK** that provides payments capabilities using the IDTech iOS framework to read credit card data using VP3300. Its goal is to ease integration and fix some of the most common issues.

**ClearentWrapper** is a singleton class and the main interaction point with the SDK.

You will use this class to update the SDK with the needed information to work properly : **API URL**, **API KEY** and the **PUBLIC KEY**.

**Important Note:**

The safe keeping of the **API URL**, **API KEY** and the **PUBLIC KEY** is the integrators responsibility. The SDK stores this information only in memory!

**ClearentWrapperListener** is the interface you will need to implement in order to receive updates, error and notifications from the SDK. Each method from the interface is documented in code.

**ClearentWrapperSharedPrefs** is a user default storage that holds information like currently paired reader and a list of previously paired readers. You should not save anything here the SDK handles this for you.

## Supported Android versions

The SDK supports current version of Android and two previous versions. Currently sdk versions 29, 30 and 31. With unofficial support for versions 24 throughout 29.

## Pairing a reader.

In order to perform transaction using the VP3300 card reader you will need to pair (connect) the device using Bluetooth, the Bluetooth connectivity is handled by the SDK .

In this step the SDK performs a Bluetooth search in order to discover the card readers around with the method **startSearching(searchDuration: Int? = null)** where search duration is the number of seconds the sdk will search for readers before returning them, the default value is 5 seconds. The SDK uses continuous search by default, stopping a search is done by connecting to a reader or by calling **stopSearching()**. In order for the device to be discoverable, it needs to be turned on and in range of the mobile device. The result of the Bluetooth search is a list of devices of type ReaderStatus and you will get the list from the delegate method **didFindReaders(readers: List<ReaderStatus>)**. In case no readers are found, the list will be empty.

Once you have the list of available readers the next step is to select the reader you want to connect to using the **selectReader(reader: ReaderStatus, tryConnect: Boolean = true)** method that will try to connect the reader if the 'tryConnect' variable is true. Once the SDK manages to connect to the reader the delegate method **deviceDidConnect()** will get called indicating the connection was successful.


## Performing a transaction

There are two ways to perform a transaction : using a card reader or by using the card details directly.

**1.Performing a transaction using the card reader.**

A transaction is performed in two steps :

1. Reading the card, the IDTech framework reads the card info and provides a jwt (token).
2. Performing an API call that will send the transaction information together with the JWT token to a payment gateway.

You can start a transaction using startTransaction(saleEntity: SaleEntity, manualEntryCardInfo: ManualEntryCardInfo? = null) method. You need to provide a SaleEntity that will contain the amount, you can also specify a tip and client related information. Providing a ManualEntryCardInfo will start a manual transaction with the card data provided in aforementioned class.

When you call the startTransaction method the SDK will start guide you to the process by calling two important methods from the ClearentWrapperDataSource  :

1. **userActionNeeded(action: UserAction)** , indicates that the user needs to do an action like swiping the card, removing the card etc.
2. **didReceiveInfo(info: UserInfo)**, this method presents different information related to the transaction.

After the transaction is completed the delegate method didFinishTransaction(response: TransactionResponse?, error: ResponseError?) will get called. You can check the error parameter to know if the transaction was successful or not.

**2. Performing a transaction using manual card entry.**

You can start a transaction using startTransaction(saleEntity: SaleEntity, manualEntryCardInfo: ManualEntryCardInfo?) method where the manualEntryCardInfo parameter will contain the card information.


**Cancelling , voiding and refunding a transaction**

If you started a card reader transaction and want to cancel it you can use cancelTransaction() method and after this call the card reader will be ready to take another transaction. You can use this method only before the card is read by the card reader. Once the card has been read the transaction will be performed and the transaction will be also registered by the payment gateway. In this case you can use the **voidTransaction(String transactionId)** to void the transaction you want (this will work only if the transaction was not yet processed by the gateway). Another option is to perform a refund using the **refundTransaction(String transactionToken, SaleEntity saleEntity)**.


## Getting information related to the card reader status

Sometimes you will need to request and display new information related to the reader like battery status or signal strength. You can achieve this by using the **startDeviceInfoUpdate()** method, calling this method will start fetching new information from the connected reader. To receive updates about the reader you must implement the **interface ReaderStatusListener** which has a method **fun onReaderStatusUpdate(ReaderStatus readerStatus)** that will be called. After implementing the interface you can register and unregister for updates with the methods **addReaderStatusListener(ReaderStatusListener listener)**, respectively **removeReaderStatusListener(ReaderStatusListener listener)**.


## Getting information related to previously paired readers

Each time you pair a new reader the SDK will save its information in Shared Preferences. You can get the list using the **fun getRecentlyPairedReaders(): List<ReaderStatus>** method inside the SDK wrapper.

You can check if a reader is connected by using the **public Boolean isReaderConnected()** method or by checking the **isConnected** property of the **currentReader**.



## Uploading a signature

If you want to upload a signature image after a transaction, you can use
**sendSignatureWithImage(Bitmap signature)**. After this method is called, the **didFinishSignature(@Nullable SignatureResponse signatureResponse, @Nullable ResponseError responseError)** delegate method will be called. Note that the sendSignature method will use the latest transaction ID as the ID for the signature in the API call, which will be lost on application restart.


## Relevant code snippets


**Initialisation**

```
    ClearentWrapper.INSTANCE.initializeSDK(
        getApplicationContext(),
        Constants.BASE_URL_SANDBOX,
        Constants.PUBLIC_KEY_SANDBOX,
        Constants.API_KEY_SANDBOX
    );
```

You will also need to implement the ClearentWrapperListener interface and set it as the listener for the ClearentWrapper:

```
public class ClearentDataSource implements ClearentWrapperListener {
    // implement the methods...
}

// Start listening to data
ClearentWrapper.INSTANCE.setListener(ClearentDataSource);

// Stop listening to data when the SDK is not used anymore
ClearentWrapper.INSTANCE.removeListener();
```



**Pairing a device**

Calling this method will start the process of pairing a card reader with an Android device.

```
    ClearentWrapper.INSTANCE.startSearching()
```

After the search for readers is completed the SDK will trigger a delegate method.

```
    @Override
    public void didFindReaders(@NonNull List<ReaderStatus> list) {
        // you can display the list of readers on the UI
    }
```

If no available readers around are found the SDK will pass in an empty list.


After the user selects one of the readers from the list you need to tell the SDK to connect to it.

```
   // readerStatus is a ReaderStatus item
   ClearentWrapper.INSTANCE.selectReader(readerStatus)
```

The SDK will try to connect to the selected device and it will call the ```deviceDidConnect()``` method when a successful connection is established.
Now you can use the paired reader to start performing transactions.


**Performing a transaction**

Using a card reader

```
    // Define a SaleEntity
    SaleEntity saleEntity = new SaleEntity(
        SaleEntity.Companion.formatAmount(22.0),
        null,  // you can also add client information
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    ClearentWrapper.INSTANCE.startTransaction(saleEntity, null);
```

Using manual card entry

```
    // Define a SaleEntity
    SaleEntity saleEntity = new SaleEntity(
        SaleEntity.Companion.formatAmount(22.0), // chargeAmount
        SaleEntity.Companion.formatAmount(5.0),  // tipAmount
        null,  // you can also add client information
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

   // Create a manual card entry instance
   ManualEntryCardInfo manualEntry = new ManualEntryCardInfo("4111111111111111", "0932", "999")
   
   ClearentWrapper.INSTANCE.startTransaction(saleEntity, manualEntryCardInfo)
```

After starting a transaction feedback messages will be triggered on the delegate.


User action needed indicates that the user/client needs to perform an action in order for the transaction to continue e.g. Insert the card.
```
    @Override
    public void userActionNeeded(@NonNull UserAction userAction) {
        // here you should check the user action type and display the informtion to the users
    }
```


User info contains information related to the transaction status e.g. Processing

```
    @Override
    public void didReceiveInfo(@NonNull UserInfo userInfo) {
        // you should display the information to the users
    }
```


After the transaction is processed a delegate method will inform you about the status.

```
    @Override
    public void didFinishTransaction(@Nullable TransactionResponse transactionResponse, @Nullable ResponseError responseError) {
        if (responseError == null) {
           // no error
        } else {
           // you should inform about the error
        }
    }
```


Kotlin example of the Clearent Wrapper integration integration [Java Example](https://github.com/clearent/ClearentWrapperDemo/tree/Java).
