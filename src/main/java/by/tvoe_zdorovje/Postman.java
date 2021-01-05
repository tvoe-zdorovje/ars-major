package by.tvoe_zdorovje;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class Postman {
    private static final Map<String, String> mailjetData = StorageManager.getMailjetData();
    private static final MailjetClient client = new MailjetClient(mailjetData.get("MJ_KEY"), mailjetData.get("MJ_VALUE"), new ClientOptions("v3.1"));

    private static final String HTML_BODY_TEMPLATE =
            "<h3>Новое сообщение с сайта arsmajor.by</h3><br><br><h4>Имя: <b>name</b><br>Телефон: <b>phone</b><br>Сообщение:</h4><br>message";

    // https://dev.mailjet.com/email/guides/send-api-v31/#send-with-attached-files
    public static void sendEmail(Map<String, String> fields, List<JSONObject> files) throws MailjetSocketTimeoutException, MailjetException {
        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(buildMessage(fields, files)));

        MailjetResponse response = client.post(request);

        int status = response.getStatus();
        if (status < 200 || status > 299) throw new MailjetException(response.getData().toString());
    }

    private static JSONObject buildMessage(Map<String, String> fields, List<JSONObject> files) {
        String HTML = HTML_BODY_TEMPLATE;

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            HTML = HTML.replace(entry.getKey(), entry.getValue());
        }

        JSONObject message = new JSONObject()
                .put(Emailv31.Message.FROM, new JSONObject()
                        .put("Email", mailjetData.get("FROM"))
                        .put("Name", "arsmajor.by"))
                .put(Emailv31.Message.TO, new JSONArray()
                        .put(new JSONObject()
                                .put("Email", mailjetData.get("TO"))
                                .put("Name", "Roma")))
                .put(Emailv31.Message.SUBJECT, "[arsmajor.by] Новое сообщение")
                .put(Emailv31.Message.HTMLPART, HTML);

        if (!fields.isEmpty()) {
            JSONArray attachments = new JSONArray();
            files.forEach(attachments::put);
            message.put(Emailv31.Message.ATTACHMENTS, attachments);
        }

        return message;
    }
}
