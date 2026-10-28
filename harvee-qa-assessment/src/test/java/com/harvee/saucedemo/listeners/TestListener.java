package com.harvee.saucedemo.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.harvee.saucedemo.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {
    private static final ExtentReports EXTENT = new ExtentReports();
    private static final ThreadLocal<ExtentTest> TEST_NODE = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        ExtentSparkReporter spark = new ExtentSparkReporter("target/reports/ExtentReport.html");
        spark.config().setReportName("SauceDemo Automation Assessment Report");
        spark.config().setDocumentTitle("Harvee QA Assessment Execution Report");
        EXTENT.attachReporter(spark);
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        TEST_NODE.set(EXTENT.createTest(testName, description));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TEST_NODE.get().pass("Test passed successfully.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverManager.getDriver();
        if (driver instanceof TakesScreenshot screenshotDriver) {
            try {
                byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path screenshotDir = Paths.get("target", "screenshots");
                Files.createDirectories(screenshotDir);
                Path filePath = screenshotDir.resolve(result.getMethod().getMethodName() + "_" + timestamp + ".png");
                Files.write(filePath, screenshotBytes);

                String base64 = screenshotDriver.getScreenshotAs(OutputType.BASE64);
                TEST_NODE.get().fail(result.getThrowable(),
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64, "Failure Screenshot").build());
            } catch (IOException e) {
                TEST_NODE.get().fail("Failed to save failure screenshot: " + e.getMessage());
            }
        } else {
            TEST_NODE.get().fail(result.getThrowable());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        EXTENT.flush();
    }
}
