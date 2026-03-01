package tests;

import com.codeborne.selenide.Configuration;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

@Epic("UI")
@Feature("Wikipedia")
@Tag("ui")
public class UITest {

    @BeforeAll
    static void setup() {

        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");

        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            Configuration.remote = remoteUrl;
        }

        Configuration.browser = "chrome";
        Configuration.headless = Boolean.parseBoolean(
                System.getProperty("headless", "true")
        );
        Configuration.browserSize = "1920x1080";
    }

    @Test
    @Tag("smoke")
    @Severity(SeverityLevel.CRITICAL)
    @Description("UI smoke test for PR")
    void wikipediaSmokeTest() {
        open("https://ru.wikipedia.org/wiki/Заглавная_страница");
        $(byText("Википедию")).shouldBe(visible);
    }

    @Test
    @Tag("regression")
    @Severity(SeverityLevel.NORMAL)
    @Description("UI regression test for nightly")
    void wikipediaSearchTest() {
        open("https://ru.wikipedia.org/wiki/Заглавная_страница");
        $("#searchInput").setValue("Selenide").pressEnter();
        $(byText("Selenide")).shouldBe(visible);
    }

    @Test
    @Tag("e2e")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Manual E2E test")
    void e2eTest() {
        open("https://example.com");
    }
}