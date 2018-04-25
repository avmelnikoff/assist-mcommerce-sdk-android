Assist Mobile SDK

Проект содержит SDK и пример приложения для проведения платежей через платежный шлюз Ассист.

Реализована поддержка платежей через web сервис Assist https://server_name/pay/order.cfm 
с отображением информации о ходе платежа в WebView и платежей через сервис https://server_name/pay/tokenpay.cfm
с использованием InApp Android Pay.

Процесс проведения платежа контролируется экземпляром класса AssistPayEngine.
Перед началом платежа требуется установить адрес сервера для обработки платежа и
слушатель результата работы экземпляра AssistPayEngine, соответствующимим методами:
 - setServerURL()
 - setEngineListener()

При запуске платежа методом payWeb или payToken на вход подается информация о платеже в экземпляре класса AssistPaymentData.

При оплате через web сервис реализована возможность ввода номера банковской карты с помощью камеры смартфона.
Для этого в проекте используется библиотека card.io.

В примере приложения представлен вариант заполенния AssistPaymentData и 
первичной инициализации AssistPaymentEngine в классе MainActivity.
В класcе ConfirmationActivity представлена окончательная инциализация AssistPaymentEngine и запуск платежа.
Так же здесь представлен пример работы с кошельком Google в рамках работы с AndroidPay.

Для работы с AndroidPay рекомендуется предварительно ознакомиться с документацией на сайте разработчика
https://developers.google.com/android-pay/.

На данный момент работа с кошельком Google представлена в режиме SANDBOX.
Поэтому для определения возможности проведения тестового платежа требуется связаться
со службой поддержки Ассист support@assist.ru

*SamsungPay support*
To use SamsungPay in your app, you need register your company on Samsung web site, register your app in samsung and download and use in your app SamsungPay SDK. See http://www.samsung.com/ru/apps/mobile/samsungpay/
Then you need create and sign certificate in samsung and provide it to assist through support@assist.ru with your merchant account to activate SamsungPay for your account in assist.
In your application you should use SamsungPay SDK to start payment with SamsungPay as it described in documentation wich you get from Samsung.
When you get payment data from SamsungPay you need to use AssistPayEngine.payToken() to start payment in Assist.
