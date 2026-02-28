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
@Feature("Wikipedia Main Page")
@Tag("ui")
public class UITest {

    @BeforeAll
    static void setup() {
        Configuration.remote = System.getenv("SELENIUM_REMOTE_URL");
        Configuration.headless = Boolean.parseBoolean(
                System.getProperty("headless", "true")
        );
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
    }

    @Test
    @Tag("smoke")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("QA Team")
    @Description("Verify Wikipedia main page loads correctly")
    void wikipediaMainPageTest() {

        open("https://ru.wikipedia.org/wiki/Заглавная_страница");

        $(byText("Википедию")).shouldBe(visible);
    }
}