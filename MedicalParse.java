package parsing_medical;

import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.jsoup.Jsoup;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static web_driver.Driver.driver;

public class MedicalParse {

    public static final WebDriver driver = driver("win_chrome");
    public static final int web_page_count = page_count();
    //public static final int web_page_count = 30;

    public static int page_count() {

        Document doc = null;
        try {
            doc = Jsoup.connect("https://spb.zoon.ru/medical/type/detskaya_poliklinika/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(doc.select("span.new_filters_block__count").text().replaceAll("[^0-9]", ""));
    }

    public static void main(String[] args) throws IOException {

        //get_site("https://spb.zoon.ru/medical/type/detskaya_poliklinika/", "urls.txt");
        site_title("src/main/resources/parsing_medical_urls/urls.txt", "title.json");
        //test_function("https://spb.zoon.ru/medical/detskaya_gorodskaya_bolnitsa_2_svyatoj_marii_magdaliny_na_14_linii/",  "test.json");
        //test_function("https://spb.zoon.ru/medical/reabilitatsionnyj_tsentr_almadeya/",  "test1.json");

    }

    public static void get_site(String url, String file_save_name) {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get(url);

            String s = driver.findElement(By.cssSelector("span.new_filters_block__count")).getText();
            if (s.indexOf(" ") > 0) s = s.substring(0, s.indexOf(" "));
            int page_count = Integer.parseInt(s);
            int scroll_count = page_count / 30;

            for (int i = 0; i < scroll_count; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
                Thread.sleep(1000);
            }

            List<WebElement> elements = driver.findElements(By.cssSelector("ul li h2 a"));

            try (FileWriter writer = new FileWriter("src/main/resources/parsing_medical_urls/" + file_save_name)) {
                for (WebElement element : elements) {
                    writer.write(element.getAttribute("href") + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

//            List<String> urls = new ArrayList<>();
//            for(WebElement element : elements) {
////                System.out.println(element.getAttribute("href"));
////                driver.get(element.getAttribute("href"));
////                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".service-page-header span[itemprop]")));
////                title.add(driver.findElement(By.cssSelector(".service-page-header span[itemprop]")).getText());
//                urls.add(element.getAttribute("href"));
//
//            }
//
//            for (String urls_ : urls){
//                driver.get(urls_);
//                elements.add(driver.findElement(By.cssSelector(".service-page-header span[itemprop]")));
//            }
//
//            for (WebElement el : elements){
//                System.out.println(el.getText());
//            }
            //driver.get(urls_.getAttribute("href"));
            //System.out.println(title.get(0));

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            driver.close();
            driver.quit();
        }

    }

    public static void site_title(String file, String title_file_name_and_format) {
        WebDriverWait wait = new WebDriverWait(driver, 30);

        try (FileWriter writer = new FileWriter("src/main/resources/parsing_medical_urls/" + title_file_name_and_format)) {

            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();

            writer.write("[\n");

            int a = 0;
            while (a < web_page_count) {
                a++;

                try {
                    driver.get(line);
                } catch (TimeoutException ignore) {
                }

                driver.manage().timeouts().pageLoadTimeout(1000, TimeUnit.MILLISECONDS);
                writer.write("  {\n     \"href\": \"" + line + "\",\n");
                writer.write("     \"title\": \"" + driver.findElement(By.cssSelector("h1 span[itemprop]")).getText() + "\",\n");


                try {
                    writer.write("     \"address\": \"" + driver.findElement(By.cssSelector(".mg-bottom-xs:nth-of-type(2) .iblock")).getText().replaceAll("[a-zA-Z/_.\"<>]", "") + "\",\n");
                } catch (Exception e) {
                    writer.write("None\"\n,");
                }


                List<WebElement> phones_number = driver.findElements(By.cssSelector(".service-phones-box span a"));

                try {

                    if (!phones_number.isEmpty()) {
                        if (phones_number.size() == 1) {

                            writer.write("     \"phone number\": \"" + phones_number.get(0).getAttribute("href").replaceAll("tel:", "") + "\"\n");
                            if (a < web_page_count) writer.write("  },\n");
                            else writer.write("  }\n");

                        } else {

                            writer.write("     \"phone numbers\": [\n");
                            int i = 0;
                            for (WebElement phone : phones_number) {
                                i++;
                                writer.write("          \"" + phone.getAttribute("href").replaceAll("tel:", "") + "\"");
                                if (i < phones_number.size()) writer.write(",\n");
                                else writer.write("\n");
                            }
                            i = 0;
                            if (a < web_page_count) writer.write("     ]\n  },\n");
                            else writer.write("     ]\n  }\n");
                        }
                    } else {
                        writer.write("     \"phone number\": \"none\"\n");
                        writer.write("  },\n");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                line = reader.readLine();
                System.out.printf("Итерация № %3d : %3d\n", a, web_page_count);

            }

            writer.write("]");

        } catch (IOException e) {
            System.out.println(e);
            System.out.printf("Что-то пошло не так :(, ошибка записи в %s !", title_file_name_and_format);
        } finally {
            System.out.printf("Запись завершена успешно!, файл %s готов!", title_file_name_and_format);
            driver.close();
            driver.quit();
        }

    }

    public static void test_function(String url, String title_file_name_and_format) {

        WebDriverWait wait = new WebDriverWait(driver, 3);

        try (FileWriter writer = new FileWriter("src/main/resources/parsing_medical_urls/" + title_file_name_and_format)) {

            writer.write("[\n");

            try {
                driver.get(url);
            } catch (TimeoutException ignore) {
            }

            driver.manage().timeouts().pageLoadTimeout(1000, TimeUnit.MILLISECONDS);

            writer.write("  {\n     \"href\": \"" + url + "\",\n");
            writer.write("     \"title\": \"" + driver.findElement(By.cssSelector("h1 span[itemprop]")).getText() + "\",\n");

            try {
                writer.write("     \"address\": \"" + driver.findElement(By.cssSelector(".mg-bottom-xs:nth-of-type(2) .iblock")).getText().replaceAll("[a-zA-Z/_.\"<>]", "") + "\",\n");
            } catch (Exception e) {
                writer.write("None\"\n,");
            }

            List<WebElement> phones_number = driver.findElements(By.cssSelector(".service-phones-box span a"));

            writer.write("     \"phone numbers\": ");

            if (!phones_number.isEmpty()) {
                writer.write("\"" + phones_number.get(0).getAttribute("href").replaceAll("tel:", "") + "\"\n");
            } else {
                writer.write("\"none\"\n");
            }
            writer.write("  }\n");
            writer.write("]");

        } catch (IOException e) {
            System.out.println(e);
        } finally {
            driver.close();
            driver.quit();
        }

    }

}
