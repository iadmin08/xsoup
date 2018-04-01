package us.codecraft.xsoup.xevaluator;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.xsoup.XElements;
import us.codecraft.xsoup.XPathEvaluator;
import us.codecraft.xsoup.Xsoup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DelayedXPathEvaluator implements XPathEvaluator {

    private String xpathStr;
    private boolean hasAtrtibute;

    public DelayedXPathEvaluator(String xpathStr){
        this.xpathStr = xpathStr;
        hasAtrtibute = false;
    }

    @Override
    public XElements evaluate(final Element element) {
        //split the xpath formatted string into sections so that we can freely parse and perform hierarchy changes.
        List<String> xPathStrList = Arrays.asList(xpathStr.split("\\.\\."));
        Elements currentElements = new Elements(new ArrayList<Element>(){{add(element);}});
        XElements lastXElements = null;

        for(int i=0; i<xPathStrList.size(); i++ ){
            String intermediateXpathStr = xPathStrList.get(i);

            //go up one in the hierarchy and move on to next iteration or
            //sanitize data that starts with /
            if(intermediateXpathStr.equals("/")){
                Element parent = currentElements.remove(0).parent();
                currentElements.add(0, parent);
                continue;
            } else if(!intermediateXpathStr.startsWith("//") && intermediateXpathStr.startsWith("/")){
                intermediateXpathStr = intermediateXpathStr.substring(1);
            }

            //Perform compilation and evaluation on element and string.
            //update current element and last element.
            XPathEvaluator intermediateEvaluator = Xsoup.compile(intermediateXpathStr);
            hasAtrtibute = intermediateEvaluator.hasAttribute();
            XElements intermediateResult = intermediateEvaluator.evaluate(currentElements.get(0));
            currentElements.clear();
            currentElements.addAll(intermediateResult.getElements());
            lastXElements = intermediateResult;

            //since we split on .. we must go up one in the hierarchy at the end of each iteration except the last.
            if(i != xPathStrList.size()-1) {
                Element parent = currentElements.remove(0).parent();
                currentElements.add(0, parent);
            }
        }
         return lastXElements;
    }

    @Override
    public boolean hasAttribute() {
        return hasAtrtibute;
    }
}
