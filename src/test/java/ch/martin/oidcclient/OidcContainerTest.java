package ch.martin.oidcclient;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;
import static org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat.MP4;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class OidcContainerTest {

    private static final String ISSUER_URI = "http://keycloak:8080/realms/users";
    private static final String EMAIL = "max.mueller@test.org";
    private static final String FIRST_NAME = "Max";
    private static final String LAST_NAME = "Müller";
    private static final String PASSWORD = "666666";
    private static final Network network = Network.newNetwork();

    @Container
    private static final KeycloakContainer keycloak = new KeycloakContainer()
            .withNetwork(network)
            .withNetworkAliases("keycloak")
            .withAdminUsername("admin")
            .withAdminPassword("admin")
            .withRealmImportFile("users-realm.json");

    private static final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
    @Container
    private static final OidcClientContainer oidcClientApp = new OidcClientContainer()
            .withNetwork(network)
            .withNetworkAliases("oidc")
            .dependsOn(keycloak)
            .withClientId("oidctest")
            .withClientSecret("ZyJ1MhdaTeviCsk7p0YFiodXjnphEYmw")
            .withIssuerUri(ISSUER_URI);

    @Container
    public static final BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
            .withCapabilities(new ChromeOptions())
            .withRecordingMode(RECORD_ALL, new File("target"), MP4);

    private static RemoteWebDriver driver;

    static {
        chrome.withNetwork(network);
        chrome.withNetworkAliases("selenium");
    }
    @SneakyThrows
    @Test
    @DisplayName("Trigger OIDC Authentication")
    @Order(0)
    void triggerOidcAuthentication() {
        oidcClientApp.followOutput(logConsumer);
        // given
        System.out.println("Keycloak runs at: " + keycloak.getAuthServerUrl());
        System.out.println("OIDC runs at: http://" + oidcClientApp.getHost() + ":" + oidcClientApp.getFirstMappedPort() + "/user");

        driver = new RemoteWebDriver(chrome.getSeleniumAddress(), new ChromeOptions());
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        // when
        driver.get("http://oidc:" + OidcClientContainer.DEFAULT_HTTP_PORT + "/user");

        // then
        WebElement input = driver.findElement(By.xpath("//input[@id='username']"));
        assertThat(input).isNotNull();
    }

    @SneakyThrows
    @Test
    @DisplayName("Register User")
    @Order(1)
    void registerUser() {
        WebElement link = driver.findElement(By.xpath("//a[starts-with(@href,'/realms/users/login-actions/registration')]"));
        link.click();
        WebElement firstname = driver.findElement(By.xpath("//input[@id='firstName']"));
        firstname.sendKeys(FIRST_NAME);
        WebElement lastname = driver.findElement(By.xpath("//input[@id='lastName']"));
        lastname.sendKeys(LAST_NAME);
        WebElement email = driver.findElement(By.xpath("//input[@id='email']"));
        email.sendKeys(EMAIL);
        WebElement password = driver.findElement(By.xpath("//input[@id='password']"));
        password.sendKeys(PASSWORD);
        WebElement passwordConfirm = driver.findElement(By.xpath("//input[@id='password-confirm']"));
        passwordConfirm.sendKeys(PASSWORD);
        WebElement submit = driver.findElement(By.xpath("//input[@type='submit']"));
        submit.click();
    }

    @SneakyThrows
    @Test
    @DisplayName("Configure TOTP")
    @Order(3)
    void configureTotp() {
        WebElement link = driver.findElement(By.xpath("//a[(@id='mode-manual')]"));
        link.click();
        WebElement secretText = driver.findElement(By.xpath("//span[(@id='kc-totp-secret-key')]"));
        String secret = secretText.getText().replace(" ","").trim();
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        TOTPGenerator generator = new TOTPGenerator.Builder(bytes)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA1); // SHA256 and SHA512 are also supported
                })
                .withPeriod(Duration.ofSeconds(30))
                .build();
        String totp = generator.now();
        System.out.println("totp " + totp);

        WebElement totpInput = driver.findElement(By.xpath("//input[(@id='totp')]"));
        totpInput.sendKeys(totp);

        Thread.sleep(3000);
        WebElement submit = driver.findElement(By.xpath("//input[(@id='saveTOTPBtn')]"));
        submit.click();
    }

    @SneakyThrows
    @Test
    @DisplayName("Verify ID Token Claims")
    @Order(4)
    void verifyToken() {
        WebElement email = driver.findElement(By.xpath("//td[@id='claim_email']"));
        WebElement firstName = driver.findElement(By.xpath("//td[@id='claim_given_name']"));
        WebElement lastName = driver.findElement(By.xpath("//td[@id='claim_family_name']"));
        WebElement issuerUri = driver.findElement(By.xpath("//td[@id='claim_iss']"));

        assertThat(email.getText()).isEqualTo(EMAIL);
        assertThat(firstName.getText()).isEqualTo(FIRST_NAME);
        assertThat(lastName.getText()).isEqualTo(LAST_NAME);
        assertThat(issuerUri.getText()).isEqualTo(ISSUER_URI);
    }
}
