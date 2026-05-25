package com.saurav.financemanager;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PersonalFinanceApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullApiFlowUsesSessionAndCalculatesGoalsAndReports() throws Exception {
        String username = uniqueEmail("flow");
        String freelanceCategory = uniqueCategory("Freelance");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "password123",
                                  "fullName": "Flow User",
                                  "phoneNumber": "+1234567890"
                                }
                                """.formatted(username)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").exists());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "password123"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[?(@.name == 'Salary')]").exists())
                .andExpect(jsonPath("$.categories[?(@.name == 'Food')]").exists());

        mockMvc.perform(post("/api/categories")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "INCOME"
                                }
                                """.formatted(freelanceCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(freelanceCategory))
                .andExpect(jsonPath("$.isCustom").value(true));

        mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 3000.00,
                                  "date": "2024-01-15",
                                  "category": "Salary",
                                  "description": "January salary"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("INCOME"));

        mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 400.00,
                                  "date": "2024-01-16",
                                  "category": "Food",
                                  "description": "Groceries"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EXPENSE"));

        mockMvc.perform(post("/api/goals")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "goalName": "Emergency Fund",
                                  "targetAmount": 5000.00,
                                  "targetDate": "2030-01-01",
                                  "startDate": "2024-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentProgress").value(2600.00))
                .andExpect(jsonPath("$.progressPercentage", closeTo(52.00, 0.01)))
                .andExpect(jsonPath("$.remainingAmount").value(2400.00));

        mockMvc.perform(get("/api/reports/monthly/2024/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(400.00))
                .andExpect(jsonPath("$.netSavings").value(2600.00));

        mockMvc.perform(get("/api/reports/yearly/2024").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(400.00))
                .andExpect(jsonPath("$.netSavings").value(2600.00));

        mockMvc.perform(get("/api/transactions")
                        .session(session)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions.length()").value(2));

        mockMvc.perform(get("/api/transactions")
                        .session(session)
                        .param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].category").value("Food"));
    }

    @Test
    void protectedApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void rootHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Personal Finance Manager API is running"));
    }

    @Test
    void userCategoryTransactionAndGoalErrorPathsReturnExpectedStatuses() throws Exception {
        String username = uniqueEmail("errors");
        String travelCategory = uniqueCategory("Travel");
        MockHttpSession session = registerAndLogin(username);

        mockMvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "password123",
                                  "fullName": "Errors User",
                                  "phoneNumber": "+1234567890"
                                }
                                """.formatted(username)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "wrongpassword"
                                }
                                """.formatted(username)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        mockMvc.perform(post("/api/categories")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "EXPENSE"
                                }
                                """.formatted(travelCategory)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "EXPENSE"
                                }
                                """.formatted(travelCategory)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category already exists"));

        mockMvc.perform(delete("/api/categories/Food").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Default categories cannot be deleted"));

        MvcResult transactionResult = mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "date": "2024-03-01",
                                  "category": "%s",
                                  "description": "Train ticket"
                                }
                                """.formatted(travelCategory)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer transactionId = readInt(transactionResult, "$.id");

        mockMvc.perform(delete("/api/categories/" + travelCategory).session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category is currently referenced by transactions"));

        mockMvc.perform(put("/api/transactions/" + transactionId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 120.00,
                                  "description": "Updated ticket"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(120.00))
                .andExpect(jsonPath("$.date").value("2024-03-01"));

        mockMvc.perform(get("/api/transactions")
                        .session(session)
                        .param("startDate", "2024-03-31")
                        .param("endDate", "2024-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date"));

        mockMvc.perform(delete("/api/transactions/" + transactionId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

        mockMvc.perform(delete("/api/categories/" + travelCategory).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        mockMvc.perform(get("/api/transactions/" + transactionId).session(session))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put("/api/transactions/" + transactionId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 130.00
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found"));

        MvcResult goalResult = mockMvc.perform(post("/api/goals")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "goalName": "Vacation",
                                  "targetAmount": 1000.00,
                                  "targetDate": "2030-06-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.remainingAmount", greaterThanOrEqualTo(0.0)))
                .andReturn();

        Integer goalId = readInt(goalResult, "$.id");

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals").isArray());

        mockMvc.perform(get("/api/goals/" + goalId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalName").value("Vacation"));

        mockMvc.perform(put("/api/goals/" + goalId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetAmount": 1500.00,
                                  "targetDate": "2031-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(1500.00))
                .andExpect(jsonPath("$.targetDate").value("2031-01-01"));

        mockMvc.perform(delete("/api/goals/" + goalId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));

        mockMvc.perform(get("/api/goals/" + goalId).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found"));

        mockMvc.perform(get("/api/reports/monthly/2024/13").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));
    }

    @Test
    void validationErrorsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "not-an-email",
                                  "password": "short",
                                  "fullName": "",
                                  "phoneNumber": "abc"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    private MockHttpSession registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "password123",
                                  "fullName": "Test User",
                                  "phoneNumber": "+1234567890"
                                }
                                """.formatted(username)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "password123"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private Integer readInt(MvcResult result, String jsonPath) throws Exception {
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }

    private String uniqueCategory(String prefix) {
        return prefix + System.nanoTime();
    }
}
