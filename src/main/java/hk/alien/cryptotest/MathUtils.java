package hk.alien.cryptotest;

public class MathUtils {
    /**
     * Calculate cumulative normal distribution function
     *
     * Credit : https://www.codeproject.com/Messages/2622967/Re-NORMSDIST-function.aspx
     */
    public static double cndf(double x) {
        int neg = (x < 0) ? 1 : 0;
        if ( neg == 1)
            x *= -1;

        double k = (1.0 /(1.0 + 0.2316419 * x));
        double y=((((1.330274429*k-1.821255978)*k+1.781477937)*k-0.356563782)*k+0.319381530)*k;
        y = 1.0 - 0.398942280401* Math.exp(-0.5*x*x)*y;

        return (1-neg)*y + neg*(1-y);
    }
}
