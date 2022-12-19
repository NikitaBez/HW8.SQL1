package ru.netology.web.banklogin.test;

import org.junit.jupiter.api.*;
import ru.netology.web.banklogin.data.DataHelper;
import ru.netology.web.banklogin.data.SQLHelper;
import ru.netology.web.banklogin.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class BankLoginTests {

    DataHelper.AutInfo user;
    DataHelper.VerifyCode code;
    LoginPage loginPage;

    @BeforeEach
    public void setUp() {
        open("http://localhost:9999");
        user = DataHelper.getAuthInfo();
        loginPage = new LoginPage();
    }

    @AfterEach
    public void setDown() {
        SQLHelper.reloadVerifyCodeTable();
        SQLHelper.setUserStatus(user.getLogin(), "active");
    }

    @AfterAll
    static void setDownClass() {
        SQLHelper.setDown();
    }

    @DisplayName("Successful authorization")
    @Test
    public void loginVerification() {
        loginPage.insert(user.getLogin(), user.getPassword());
        var verifyPage = loginPage.success();
        code = DataHelper.getValidCode(user.getLogin());
        verifyPage.insert(code.getVerifyCode());
        var dashboardPage = verifyPage.success();
    }

    @DisplayName("Authorization denied with an outdated confirmation code")
    @Test
    public void shouldNoAuthWithOldestVerifyCode() {
        loginPage.insert(user.getLogin(), user.getPassword());
        var verifyPage = loginPage.success();
        code = DataHelper.getValidCode(user.getLogin());
        verifyPage.insert(code.getVerifyCode());
        var dashboardPage = verifyPage.success();

        open("http://localhost:9999");
        loginPage.insert(user.getLogin(), user.getPassword());
        verifyPage = loginPage.success();
        verifyPage.insert(code.getVerifyCode());
        verifyPage.failed();
    }

    @DisplayName("Authorization denied with random verification code")
    @Test
    public void shouldNoAuthWithInvalidVerifyCode() {
        loginPage.insert(user.getLogin(), user.getPassword());
        var verifyPage = loginPage.success();
        code = DataHelper.getRandomCode();
        verifyPage.insert(code.getVerifyCode());
        verifyPage.failed();
    }

    @DisplayName("Authorization denied with invalid login")
    @Test
    public void shouldNoAuthWithInvalidLogin() {
        var login = DataHelper.getRandomLogin();
        loginPage.insert(login, user.getPassword());
        loginPage.failed();
    }

    @DisplayName("Login denied with invalid password")
    @Test
    public void shouldNoAuthWithInvalidPassword() {
        var password = DataHelper.getRandomPassword();
        loginPage.insert(user.getLogin(), password);
        loginPage.failed();
    }

    @DisplayName("User lockout after 3 wrong password attempts")
    @Test
    public void shouldBlockUserAfterThreeInputInvalidPassword() {
        var password = DataHelper.getRandomPassword();
        loginPage.insert(user.getLogin(), password);
        loginPage.failed();

        open("http://localhost:9999");
        password = DataHelper.getRandomPassword();
        loginPage.insert(user.getLogin(), password);
        loginPage.failed();

        open("http://localhost:9999");
        password = DataHelper.getRandomPassword();
        loginPage.insert(user.getLogin(), password);
        loginPage.failed();

        assertEquals("blocked", SQLHelper.getUserStatus(user.getLogin()));

        open("http://localhoct:9999");
        loginPage.insert(user.getLogin(), user.getPassword());
        loginPage.blocked();
    }

    @DisplayName("Message about an empty field Login")
    @Test
    public void shouldNotificationWithEmptyLogin() {
        loginPage.insert(null, user.getPassword());
        loginPage.emptyLogin();
    }

    @DisplayName("Message about an empty field Password")
    @Test
    public void shouldNotificationWithEmptyPassword() {
        loginPage.insert(user.getLogin(), null);
        loginPage.emptyPassword();
    }

    @DisplayName("Message about an empty verification code field")
    @Test
    public void shouldNotificationWithEmptyVerifyCode() {
        loginPage.insert(user.getLogin(), user.getPassword());
        var verifyPage = loginPage.success();
        verifyPage.insert(null);
        verifyPage.emptyCode();
    }
}