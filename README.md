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

**Поддержка SamsungPay**

Для использования SamsungPay в вашем приложении, вам необходимо зарегистрироваться в Samsung и зарегистрировать свое приложение, затем получить SamsungPay SDK. Смотрите http://www.samsung.com/ru/apps/mobile/samsungpay/
Затем вам нужно создать запрос на сертификат и выпустить сертификат магазина в Samsung и передать его в assist для подключения услуги SamsungPay вашему аккаунту через support@assist.ru.
В вашем приложении вы должны следовать инструкции Samsung для инициации платежа через SamsungPay.
Для завершения оплаты SamsungPay вам необходимо передать данные полученные из SamsungPay SDK в Assist через функцию  AssistPayEngine.payToken().
