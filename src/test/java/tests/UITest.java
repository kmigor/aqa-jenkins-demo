package tests;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class UITest {
    @Test
    @Tag("smoke")
    void googleTitleTest(){
        Configuration.headless = true;
        open("https://ru.wikipedia.org/wiki/");
        $(byText("Википедию")).shouldBe(visible);
    }
}
