package bill;

import java.io.*;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Persists company-level settings (name, contact, GST rate) between application runs.
 * Settings are stored in a simple .properties file in the user's home directory.
 */
public class AppSettings {
    private static final String SETTINGS_FILE = System.getProperty("user.home") + "/.ridhisidhi.properties";

    private static AppSettings instance;

    private String companyName    = "Ridhi Sidhi Tours";
    private String companyTagline = "Your Journey, Our Responsibility";
    private String companyEmail   = "info@ridhisidhitours.com";
    private String companyPhone   = "+91-9800000000";
    private BigDecimal gstRate    = new BigDecimal("18");

    private AppSettings() { load(); }

    public static AppSettings getInstance() {
        if (instance == null) instance = new AppSettings();
        return instance;
    }

    private void load() {
        File f = new File(SETTINGS_FILE);
        if (!f.exists()) return;
        try (FileInputStream fis = new FileInputStream(f)) {
            Properties p = new Properties();
            p.load(fis);
            companyName    = p.getProperty("company.name",    companyName);
            companyTagline = p.getProperty("company.tagline", companyTagline);
            companyEmail   = p.getProperty("company.email",   companyEmail);
            companyPhone   = p.getProperty("company.phone",   companyPhone);
            gstRate        = new BigDecimal(p.getProperty("gst.rate", gstRate.toPlainString()));
        } catch (Exception ignored) {}
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("company.name",    companyName);
        p.setProperty("company.tagline", companyTagline);
        p.setProperty("company.email",   companyEmail);
        p.setProperty("company.phone",   companyPhone);
        p.setProperty("gst.rate",        gstRate.toPlainString());
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            p.store(fos, "Ridhi Sidhi Tours Settings");
        } catch (Exception ignored) {}
    }

    public void applyTo(BookingSession session) {
        session.setCompanyDetails(companyName, companyTagline, companyEmail, companyPhone);
        session.setGstRate(gstRate);
    }

    // Getters & Setters
    public String getCompanyName()       { return companyName; }
    public String getCompanyTagline()    { return companyTagline; }
    public String getCompanyEmail()      { return companyEmail; }
    public String getCompanyPhone()      { return companyPhone; }
    public BigDecimal getGstRate()       { return gstRate; }

    public void setCompanyName(String v)    { companyName    = v; }
    public void setCompanyTagline(String v) { companyTagline = v; }
    public void setCompanyEmail(String v)   { companyEmail   = v; }
    public void setCompanyPhone(String v)   { companyPhone   = v; }
    public void setGstRate(BigDecimal v)    { gstRate        = v; }
}
