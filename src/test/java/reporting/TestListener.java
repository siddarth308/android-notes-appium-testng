package reporting;

import base.BaseTest;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.OutputType;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final ExtentReports extent = new ExtentReports();
    private static final ThreadLocal<ExtentTest> current = new ThreadLocal<>();

    static {
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/NotesAppReport.html");
        spark.config().setDocumentTitle("Notes App Test Report");
        spark.config().setReportName("Notes App - Appium Tests");
        extent.attachReporter(spark);
        extent.setSystemInfo("App", "com.example.myapp");
        extent.setSystemInfo("Device", "Android Emulator");
    }

    @Override
    public void onTestStart(ITestResult r) {
        String name = r.getMethod().getMethodName();
        ExtentTest test = extent.createTest(name, r.getMethod().getDescription())
                .assignCategory(r.getTestClass().getRealClass().getSimpleName());
        for (String group : r.getMethod().getGroups()) {
            test.assignCategory(group); // e.g. "known-defect" becomes a filter in the report
        }
        current.set(test);
        System.out.println("START " + name);
    }

    @Override
    public void onTestSuccess(ITestResult r) {
        current.get().pass("Passed");
        System.out.println("PASS  " + r.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult r) {
        current.get().fail(r.getThrowable());
        try {
            BaseTest testClass = (BaseTest) r.getInstance();
            String png = testClass.getDriver().getScreenshotAs(OutputType.BASE64);
            current.get().fail("Screen at failure",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(png).build());
        } catch (Exception e) {
            System.out.println("Could not take screenshot: " + e.getMessage());
        }
        System.out.println("FAIL  " + r.getMethod().getMethodName() + " : " + r.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult r) {
        if (current.get() != null) {
            current.get().skip("Skipped");
        }
        System.out.println("SKIP  " + r.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush(); // writes the HTML file
    }
}