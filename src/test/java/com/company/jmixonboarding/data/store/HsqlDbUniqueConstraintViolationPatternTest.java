package com.company.jmixonboarding.data.store;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liangwenqi
 * @date 2024/11/28
 */
@SpringBootTest
public class HsqlDbUniqueConstraintViolationPatternTest {

    @Test
    public void testPatternMatcher() {
        String exceptionStr = "Internal Exception: java.sql.SQLIntegrityConstraintViolationException: integrity constraint violation: unique constraint or index violation ; IDX_DEPARTMENT_UNQ table: DEPARTMENT\n" +
                "Error Code: -104\n" +
                "Call: INSERT INTO DEPARTMENT (ID, NAME, VERSION, HR_MANAGER_ID) VALUES (?, ?, ?, ?)\n" +
                "\tbind => [c1438448-9c23-1cbd-c9df-6d79a91eaf32, Marketing, 1, null]\n" +
                "Query: InsertObjectQuery(com.company.jmixonboarding.entity.Department-c1438448-9c23-1cbd-c9df-6d79a91eaf32 [new,managed])";

        String uniqueConstraintViolationPattern = "integrity constraint violation: unique constraint or index violation\\s*[:;]?\\s*([^\\s]+)";
        Pattern pattern = Pattern.compile(uniqueConstraintViolationPattern);
        Matcher matcher = pattern.matcher(exceptionStr);
        if (matcher.find()) {
            System.out.println("################## => " + resolveConstraintName(matcher) + " <= ##################");
        }
    }


    protected String resolveConstraintName(Matcher matcher) {
        String constraintName = "";
        if (matcher.groupCount() == 1) {
            constraintName = matcher.group(1);
        } else {
            for (int i = 1; i < matcher.groupCount(); ++i) {
                if (StringUtils.isNotBlank(matcher.group(i))) {
                    constraintName = matcher.group(i);
                    break;
                }
            }
        }

        return constraintName.toUpperCase();
    }

}
