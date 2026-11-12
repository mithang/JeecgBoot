//package org.jeecg.modules.airag.test;
//
//import dev.langchain4j.data.document.Document;
//import dev.langchain4j.data.document.DocumentSplitter;
//import dev.langchain4j.data.document.Metadata;
//import dev.langchain4j.data.document.splitter.DocumentSplitters;
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.embedding.EmbeddingModel;
//import dev.langchain4j.model.input.PromptTemplate;
//import dev.langchain4j.model.openai.OpenAiTokenizer;
//import dev.langchain4j.rag.DefaultRetrievalAugmentor;
//import dev.langchain4j.rag.RetrievalAugmentor;
//import dev.langchain4j.rag.content.injector.DefaultContentInjector;
//import dev.langchain4j.rag.content.retriever.ContentRetriever;
//import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
//import dev.langchain4j.rag.query.router.DefaultQueryRouter;
//import dev.langchain4j.rag.query.router.QueryRouter;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.store.embedding.EmbeddingMatch;
//import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
//import dev.langchain4j.store.embedding.EmbeddingStore;
//import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
//import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.ai.assistant.AiChatAssistant;
//import org.jeecg.ai.factory.AiModelFactory;
//import org.jeecg.ai.factory.AiModelOptions;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//
//import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
//
///**
// * @Description: Kiểm thử quy trình
// * @Author: chenrui
// * @Date: 2025/2/11 16:11
// */
//@Slf4j
//public class TestVector {
//
//    String openAIBaseUrl = "https://api.v3.cm/v1";
//    String openAIApiKey = "sk-xxx";
//
//    EmbeddingModel embeddingModel;
//    EmbeddingStore<TextSegment> embeddingStore;
//
//    @Before
//    public void before() {
//        AiModelOptions.AiModelOptionsBuilder modelOpBuilder = AiModelOptions.builder().provider(AiModelFactory.AIMODEL_TYPE_OPENAI)
//                .baseUrl(openAIBaseUrl).apiKey(openAIApiKey);
//        embeddingModel = AiModelFactory.createEmbeddingModel(modelOpBuilder.build());
//        embeddingStore = PgVectorEmbeddingStore.builder()
//                // Connection and table parameters
//                .host("localhost")
//                .port(15432)
//                .database("postgres")
//                .user("postgres")
//                .password("123456")
//                .table("test_embeddings")
//                // Embedding dimension
//                .dimension(embeddingModel.dimension())      // Required: Must match the embedding model’s output dimension
//                // Indexing and performance options
//                .useIndex(true)                             // Enable IVFFlat index
//                .indexListSize(100)                         // Number of lists for IVFFlat index
//                // Table creation options
//                .createTable(true)                          // Automatically create the table if it doesn’t exist
//                .dropTableFirst(false)                      // Don’t drop the table first (set to true if you want a fresh start)
//                .build();
//    }
//
//    @Test
//    public void testSave2Vector() {
//        embeddingStore.removeAll(metadataKey("id").isEqualTo("sdfsdf"));
//        DocumentSplitter splitter = DocumentSplitters.recursive(200,
//                50,
//                new OpenAiTokenizer());
//        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
//                .documentSplitter(splitter)
//                .embeddingModel(embeddingModel)
//                .embeddingStore(embeddingStore)
//                .build();
//        Document from = Document.from(doc, Metadata.metadata("id", "sdfsdf"));
//        ingestor.ingest(from);
//    }
//
//    @Test
//    public void testQueryByVector() {
//        Embedding queryEmbedding = embeddingModel.embed("Công việc toàn thời gian và công việc bán thời gian khác nhau như thế nào?").content();
//        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
//                .queryEmbedding(queryEmbedding)
//                .maxResults(5)
//                .filter(metadataKey("id").isEqualTo("sdfsdf"))
//                .build();
//
//        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(embeddingSearchRequest).matches();
//        for (int i = 0; i < relevant.size(); i++) {
//            EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(i);
//            System.out.println("Kết quả:" + i + "=================================================");
//            System.out.println("Điểm số:" + embeddingMatch.score()); // 0.8144288608390052
//            System.out.println("Nội dung:" + embeddingMatch.embedded().text()); // I like football.
//        }
//    }
//
//
//    @Test
//    public void testQueryByRAG() {
//        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)
//                .embeddingModel(embeddingModel)
//                .maxResults(5)
//                // maxResults can also be specified dynamically depending on the query
////                .dynamicMaxResults(query -> 3)
//                .minScore(0.75)
//                // minScore can also be specified dynamically depending on the query
////                .dynamicMinScore(query -> 0.75)
//                .filter(metadataKey("id").isEqualTo("sdfsdf"))
//                // filter can also be specified dynamically depending on the query
////                .dynamicFilter(query -> {
////                    String userId = getUserId(query.metadata().chatMemoryId());
////                    return metadataKey("userId").isEqualTo(userId);
////                })
//                .build();
//
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
//        ChatLanguageModel chatModel = AiModelFactory.createChatModel(AiModelOptions.builder()
//                .baseUrl(openAIBaseUrl).apiKey(openAIApiKey).provider(AiModelFactory.AIMODEL_TYPE_OPENAI).build());
//
//        QueryRouter queryRouter = new DefaultQueryRouter(contentRetriever);
//        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder().queryRouter(queryRouter)
//                .contentInjector(DefaultContentInjector.builder()
//                        .promptTemplate(PromptTemplate.from("{{userMessage}}\n\n Dùng thông tin sau để trả lời câu hỏi:\n{{contents}}\n\n"))
//                        .build()).build();
//        AiChatAssistant assistant = AiServices.builder(AiChatAssistant.class)
//                .chatLanguageModel(chatModel)
//                .chatMemory(chatMemory)
//                .retrievalAugmentor(retrievalAugmentor)
//                .build();
//        String chat = assistant.chat("Chưa ký hợp đồng lao động?");
//        System.out.println(chat);
//
//    }
//
//
//    String doc = "Luật Hợp đồng lao động của Cộng hòa Nhân dân Trung Hoa\n" +
//            "Đã căn cứ vào 2013.07.01 sửa đổi theo bản sửa đổi đã thi hành\n" +
//            "Danh mục\n" +
//            "Chương 1　Quy tắc chung\n" +
//            "Chương 2　Ký kết hợp đồng lao động\n" +
//            "Chương 3　Thực hiện và thay đổi hợp đồng lao động\n" +
//            "Chương 4　Chấm dứt và kết thúc hợp đồng lao động\n" +
//            "Chương 5　Quy định đặc biệt\n" +
//            "Chương 6　Giám sát kiểm tra\n" +
//            "Chương 7　Trách nhiệm pháp lý\n" +
//            "Chương 8　Phụ tắc\n" +
//            "Chương 1　Quy tắc chung\n" +
//            "Điều 1　Để hoàn thiện chế độ hợp đồng lao động, làm rõ quyền và nghĩa vụ của các bên trong hợp đồng lao động, bảo vệ quyền và lợi ích hợp pháp của người lao động, xây dựng và phát triển quan hệ lao động hài hòa, ổn định, Luật này được ban hành.\n" +
//            "Điều 2　Các doanh nghiệp, tổ chức kinh tế cá thể, đơn vị phi doanh nghiệp dân lập và các tổ chức khác trong lãnh thổ Cộng hòa Nhân dân Trung Hoa(sau đây gọi là đơn vị sử dụng lao động)Việc thiết lập quan hệ lao động với người lao động, ký kết, thực hiện, thay đổi, chấm dứt hoặc kết thúc hợp đồng lao động được áp dụng theo Luật này.\n" +
//            "Cơ quan nhà nước, đơn vị sự nghiệp, đoàn thể xã hội và người lao động thiết lập quan hệ lao động với các đơn vị này khi ký kết, thực hiện, thay đổi, chấm dứt hoặc kết thúc hợp đồng lao động được thực hiện theo Luật này.\n" +
//            "Điều 3　Khi ký kết hợp đồng lao động, phải tuân theo nguyên tắc hợp pháp, công bằng, bình đẳng tự nguyện, thỏa thuận nhất trí, trung thực và đáng tin cậy.\n" +
//            "Hợp đồng lao động được ký kết theo pháp luật có hiệu lực ràng buộc, đơn vị sử dụng lao động và người lao động phải thực hiện các nghĩa vụ đã thỏa thuận trong hợp đồng lao động.\n" +
//            "Điều 4　Đơn vị sử dụng lao động phải xây dựng và hoàn thiện hệ thống quy chế lao động theo quy định của pháp luật, bảo đảm người lao động được hưởng quyền lao động và thực hiện nghĩa vụ lao động.\n" +
//            "Khi đơn vị sử dụng lao động xây dựng, sửa đổi hoặc quyết định các quy chế hoặc vấn đề quan trọng liên quan trực tiếp đến lợi ích thiết thân của người lao động như thù lao lao động, thời gian làm việc, nghỉ ngơi, an toàn vệ sinh lao động, bảo hiểm phúc lợi, đào tạo nhân viên, kỷ luật lao động và quản lý định mức lao động, phải thông qua đại hội đại biểu công nhân viên chức hoặc toàn thể công nhân viên chức thảo luận, đề xuất phương án và ý kiến, và cùng công đoàn hoặc đại diện công nhân viên chức thương lượng bình đẳng để quyết định.\n" +
//            "Trong quá trình thực hiện các quy chế và quyết định về vấn đề quan trọng, nếu công đoàn hoặc công nhân viên chức cho rằng không phù hợp, có quyền đề xuất với đơn vị sử dụng lao động để sửa đổi, hoàn thiện thông qua thương lượng.\n" +
//            "Đơn vị sử dụng lao động phải công khai các quy chế và quyết định về vấn đề quan trọng liên quan trực tiếp đến lợi ích thiết thân của người lao động, hoặc thông báo cho người lao động.\n" +
//            "Điều 5　Cơ quan hành chính lao động của chính quyền nhân dân cấp huyện trở lên cùng với công đoàn và đại diện phía doanh nghiệp thiết lập và hoàn thiện cơ chế ba bên phối hợp quan hệ lao động, cùng nhau nghiên cứu và giải quyết các vấn đề quan trọng liên quan đến quan hệ lao động.\n" +
//            "Điều 6　Công đoàn phải giúp đỡ, hướng dẫn người lao động ký kết và thực hiện hợp đồng lao động theo pháp luật với đơn vị sử dụng lao động, đồng thời thiết lập cơ chế thương lượng tập thể với đơn vị sử dụng lao động để bảo vệ quyền và lợi ích hợp pháp của người lao động.\n" +
//            "Chương 2　Ký kết hợp đồng lao động\n" +
//            "Điều 7　Đơn vị sử dụng lao động kể từ ngày bắt đầu sử dụng lao động thì thiết lập quan hệ lao động với người lao động. Đơn vị sử dụng lao động phải lập danh sách công nhân viên chức để tra cứu.\n" +
//            "Điều 8　Khi tuyển dụng người lao động, đơn vị sử dụng lao động phải thông báo trung thực cho người lao động về nội dung công việc, điều kiện làm việc, địa điểm làm việc, nguy hại nghề nghiệp, tình hình an toàn sản xuất, thù lao lao động và các tình huống khác mà người lao động yêu cầu được biết;Đơn vị sử dụng lao động có quyền tìm hiểu tình hình cơ bản của người lao động có liên quan trực tiếp đến hợp đồng lao động, người lao động phải trình bày trung thực.\n" +
//            "Điều 9　Khi tuyển dụng người lao động, đơn vị sử dụng lao động không được giữ chứng minh thư nhân dân và các giấy tờ khác của người lao động, không được yêu cầu người lao động cung cấp bảo lãnh hoặc thu tiền, tài sản của người lao động dưới bất kỳ danh nghĩa nào khác.\n" +
//            "Điều 10　Khi thiết lập quan hệ lao động, phải ký kết hợp đồng lao động bằng văn bản.\n" +
//            "Đã thiết lập quan hệ lao động nhưng chưa ký kết hợp đồng lao động bằng văn bản cùng lúc thì phải ký kết hợp đồng lao động bằng văn bản trong vòng một tháng kể từ ngày bắt đầu sử dụng lao động.\n" +
//            "Nếu đơn vị sử dụng lao động và người lao động ký kết hợp đồng lao động trước khi bắt đầu sử dụng lao động, quan hệ lao động được thiết lập kể từ ngày bắt đầu sử dụng lao động.\n" +
//            "Điều 11　Đơn vị sử dụng lao động không ký kết hợp đồng lao động bằng văn bản cùng lúc với việc sử dụng lao động, và thù lao lao động thỏa thuận với người lao động không rõ ràng thì thù lao lao động của người lao động mới tuyển dụng được thực hiện theo tiêu chuẩn quy định trong hợp đồng tập thể;Nếu không có hợp đồng tập thể hoặc hợp đồng tập thể không quy định thì thực hiện cùng công việc thì cùng mức lương.\n" +
//            "Điều 12　Hợp đồng lao động được chia thành hợp đồng lao động có thời hạn xác định, hợp đồng lao động không xác định thời hạn và hợp đồng lao động có thời hạn theo thời gian hoàn thành một công việc nhất định.\n" +
//            "Điều 13　Hợp đồng lao động có thời hạn xác định là hợp đồng lao động mà đơn vị sử dụng lao động và người lao động thỏa thuận về thời gian chấm dứt hợp đồng.\n" +
//            "Đơn vị sử dụng lao động và người lao động thỏa thuận nhất trí có thể ký kết hợp đồng lao động có thời hạn xác định.\n" +
//            "Điều 14　Hợp đồng lao động không xác định thời hạn là hợp đồng lao động mà đơn vị sử dụng lao động và người lao động thỏa thuận không có thời gian chấm dứt xác định.\n" +
//            "Đơn vị sử dụng lao động và người lao động thỏa thuận nhất trí có thể ký kết hợp đồng lao động không xác định thời hạn. Trong các trường hợp sau đây, nếu người lao động đề xuất hoặc đồng ý gia hạn, ký kết hợp đồng lao động, trừ trường hợp người lao động đề xuất ký kết hợp đồng lao động có thời hạn xác định, thì phải ký kết hợp đồng lao động không xác định thời hạn:\n" +
//            "(1)Người lao động làm việc liên tục đủ mười năm tại đơn vị sử dụng lao động đó;\n" +
//            "(2)Khi đơn vị sử dụng lao động lần đầu thực hiện chế độ hợp đồng lao động hoặc doanh nghiệp nhà nước cải cách và ký kết lại hợp đồng lao động, người lao động làm việc liên tục đủ mười năm tại đơn vị sử dụng lao động đó và còn dưới mười năm đến tuổi nghỉ hưu theo luật định;\n" +
//            "(3)Đã ký kết liên tiếp hai lần hợp đồng lao động có thời hạn xác định, và người lao động không thuộc các trường hợp quy định tại Điều 39 và khoản 1, khoản 2 Điều 40 của Luật này, khi gia hạn hợp đồng lao động.\n" +
//            "Đơn vị sử dụng lao động kể từ ngày bắt đầu sử dụng lao động đủ một năm mà không ký kết hợp đồng lao động bằng văn bản với người lao động thì được coi là đơn vị sử dụng lao động và người lao động đã ký kết hợp đồng lao động không xác định thời hạn.\n" +
//            "Điều 15　Hợp đồng lao động có thời hạn theo thời gian hoàn thành một công việc nhất định là hợp đồng lao động mà đơn vị sử dụng lao động và người lao động thỏa thuận lấy việc hoàn thành một công việc nào đó làm thời hạn hợp đồng.\n" +
//            "Đơn vị sử dụng lao động và người lao động thỏa thuận nhất trí có thể ký kết hợp đồng lao động có thời hạn theo thời gian hoàn thành một công việc nhất định.\n" +
//            "Điều 16　Hợp đồng lao động do đơn vị sử dụng lao động và người lao động thỏa thuận nhất trí, và có hiệu lực sau khi đơn vị sử dụng lao động và người lao động ký tên hoặc đóng dấu trên văn bản hợp đồng lao động.\n" +
//            "Văn bản hợp đồng lao động do đơn vị sử dụng lao động và người lao động mỗi bên giữ một bản.\n" +
//            "Điều 17　Hợp đồng lao động phải có các điều khoản sau:\n" +
//            "(1)Tên, trụ sở và người đại diện theo pháp luật hoặc người phụ trách chính của đơn vị sử dụng lao động;\n" +
//            "(2)Họ tên, địa chỉ và số chứng minh thư nhân dân hoặc số giấy tờ tùy thân hợp lệ khác của người lao động;\n" +
//            "(3)Thời hạn hợp đồng lao động;\n" +
//            "(4)Nội dung công việc và địa điểm làm việc;\n" +
//            "(5)Thời gian làm việc và nghỉ ngơi;\n" +
//            "(6)Thù lao lao động;\n" +
//            "(7)Bảo hiểm xã hội;\n" +
//            "(8)Bảo hộ lao động, điều kiện lao động và phòng hộ nguy hại nghề nghiệp;\n" +
//            "(9)Các vấn đề khác mà pháp luật, quy định pháp luật quy định phải đưa vào hợp đồng lao động.\n" +
//            "Ngoài các điều khoản bắt buộc quy định tại khoản trên, đơn vị sử dụng lao động và người lao động có thể thỏa thuận các vấn đề khác như thời gian thử việc, đào tạo, bảo mật, bảo hiểm bổ sung và phúc lợi.\n" +
//            "Điều 18　Nếu hợp đồng lao động thỏa thuận không rõ ràng về các tiêu chuẩn như thù lao lao động và điều kiện lao động gây ra tranh chấp, đơn vị sử dụng lao động và người lao động có thể thương lượng lại;Nếu thương lượng không thành, áp dụng quy định của hợp đồng tập thể;Nếu không có hợp đồng tập thể hoặc hợp đồng tập thể không quy định về thù lao lao động thì thực hiện cùng công việc thì cùng mức lương;Nếu không có hợp đồng tập thể hoặc hợp đồng tập thể không quy định các tiêu chuẩn như điều kiện lao động thì áp dụng các quy định có liên quan của Nhà nước.\n" +
//            "Điều 19　Hợp đồng lao động có thời hạn từ ba tháng trở lên và dưới một năm thì thời gian thử việc không được vượt quá một tháng;Hợp đồng lao động có thời hạn từ một năm trở lên và dưới ba năm thì thời gian thử việc không được vượt quá hai tháng;Đối với hợp đồng lao động có thời hạn xác định trên ba năm và hợp đồng lao động không xác định thời hạn, thời gian thử việc không được vượt quá sáu tháng.\n" +
//            "Cùng một đơn vị sử dụng lao động và cùng một người lao động chỉ được thỏa thuận một lần thử việc.\n" +
//            "Hợp đồng lao động có thời hạn theo thời gian hoàn thành một công việc nhất định hoặc hợp đồng lao động có thời hạn dưới ba tháng thì không được thỏa thuận thời gian thử việc.\n" +
//            "Thời gian thử việc được tính trong thời hạn hợp đồng lao động. Hợp đồng lao động chỉ thỏa thuận về thời gian thử việc thì thời gian thử việc không thành lập, thời hạn đó là thời hạn hợp đồng lao động.\n" +
//            "Điều 20　Tiền lương của người lao động trong thời gian thử việc không được thấp hơn mức lương thấp nhất của cùng vị trí trong đơn vị này hoặc 80% mức lương thỏa thuận trong hợp đồng lao động, và không được thấp hơn mức lương tối thiểu của địa phương nơi đơn vị sử dụng lao động đặt trụ sở.\n" +
//            "Điều 21　Trong thời gian thử việc, trừ trường hợp người lao động thuộc các trường hợp quy định tại Điều 39 và khoản 1, khoản 2 Điều 40 của Luật này, đơn vị sử dụng lao động không được chấm dứt hợp đồng lao động. Nếu đơn vị sử dụng lao động chấm dứt hợp đồng lao động trong thời gian thử việc thì phải giải thích lý do cho người lao động.\n" +
//            "Điều 22　Đơn vị sử dụng lao động cung cấp chi phí đào tạo chuyên môn cho người lao động để đào tạo kỹ thuật chuyên nghiệp thì có thể ký kết thỏa thuận với người lao động đó để thỏa thuận thời gian phục vụ.\n" +
//            "Người lao động vi phạm thỏa thuận về thời gian phục vụ thì phải trả tiền phạt vi phạm cho đơn vị sử dụng lao động theo thỏa thuận. Số tiền phạt vi phạm không được vượt quá chi phí đào tạo mà đơn vị sử dụng lao động đã cung cấp. Tiền phạt vi phạm mà đơn vị sử dụng lao động yêu cầu người lao động trả không được vượt quá chi phí đào tạo được phân bổ cho phần thời gian phục vụ chưa thực hiện.\n" +
//            "Việc đơn vị sử dụng lao động và người lao động thỏa thuận thời gian phục vụ không ảnh hưởng đến việc tăng thù lao lao động của người lao động trong thời gian phục vụ theo cơ chế điều chỉnh tiền lương bình thường.\n" +
//            "Điều 23　Đơn vị sử dụng lao động và người lao động có thể thỏa thuận trong hợp đồng lao động về việc giữ bí mật thương mại của đơn vị sử dụng lao động và các vấn đề bảo mật liên quan đến quyền sở hữu trí tuệ.\n" +
//            "Đối với người lao động có nghĩa vụ bảo mật, đơn vị sử dụng lao động có thể thỏa thuận điều khoản hạn chế cạnh tranh với người lao động trong hợp đồng lao động hoặc thỏa thuận bảo mật, và thỏa thuận sau khi chấm dứt hoặc kết thúc hợp đồng lao động, trong thời hạn hạn chế cạnh tranh sẽ bồi thường kinh tế hàng tháng cho người lao động. Người lao động vi phạm thỏa thuận hạn chế cạnh tranh thì phải trả tiền phạt vi phạm cho đơn vị sử dụng lao động theo thỏa thuận.\n" +
//            "Điều 24　Những người bị hạn chế cạnh tranh chỉ giới hạn ở cán bộ quản lý cấp cao, nhân viên kỹ thuật cao cấp của đơn vị sử dụng lao động và những người khác có nghĩa vụ bảo mật. Phạm vi, khu vực, thời hạn hạn chế cạnh tranh do đơn vị sử dụng lao động và người lao động thỏa thuận, thỏa thuận hạn chế cạnh tranh không được vi phạm quy định của pháp luật, quy định pháp luật.\n" +
//            "Sau khi chấm dứt hoặc kết thúc hợp đồng lao động, những người quy định tại khoản trên đến làm việc tại đơn vị sử dụng lao động khác có quan hệ cạnh tranh sản xuất hoặc kinh doanh sản phẩm cùng loại, kinh doanh cùng loại với đơn vị này, hoặc tự mở cơ sở sản xuất hoặc kinh doanh sản phẩm cùng loại, kinh doanh cùng loại thì thời hạn hạn chế cạnh tranh không được vượt quá hai năm.\n" +
//            "Điều 25　Trừ các trường hợp quy định tại Điều 22 và Điều 23 của Luật này, đơn vị sử dụng lao động không được thỏa thuận với người lao động về việc người lao động phải chịu tiền phạt vi phạm.\n" +
//            "Điều 26　Các hợp đồng lao động sau đây vô hiệu hoặc vô hiệu một phần:\n" +
//            "(1)Dùng thủ đoạn lừa dối, ép buộc hoặc lợi dụng tình thế khó khăn của người khác để khiến đối phương ký kết hoặc thay đổi hợp đồng lao động trong trường hợp trái với ý chí thực sự;\n" +
//            "(2)Đơn vị sử dụng lao động miễn trừ trách nhiệm pháp lý của mình, loại trừ quyền của người lao động;\n" +
//            "(3)Vi phạm quy định bắt buộc của pháp luật, quy định hành chính.\n" +
//            "Tranh chấp về việc hợp đồng lao động vô hiệu hoặc vô hiệu một phần do cơ quan trọng tài tranh chấp lao động hoặc tòa án nhân dân xác nhận.\n" +
//            "Điều 27　Hợp đồng lao động vô hiệu một phần, không ảnh hưởng đến hiệu lực của các phần khác, các phần khác vẫn có hiệu lực.\n" +
//            "Điều 28　Hợp đồng lao động bị xác nhận vô hiệu, người lao động đã lao động thì đơn vị sử dụng lao động phải trả thù lao lao động cho người lao động. Số tiền thù lao lao động được xác định tham chiếu thù lao lao động của người lao động ở vị trí tương tự hoặc gần tương tự trong đơn vị này.\n" +
//            "Chương 3　Thực hiện và thay đổi hợp đồng lao động\n" +
//            "Điều 29　Đơn vị sử dụng lao động và người lao động phải thực hiện đầy đủ nghĩa vụ của mình theo thỏa thuận trong hợp đồng lao động.\n" +
//            "Điều 30　Đơn vị sử dụng lao động phải trả thù lao lao động cho người lao động kịp thời và đầy đủ theo thỏa thuận trong hợp đồng lao động và quy định của Nhà nước.\n" +
//            "Đơn vị sử dụng lao động nợ hoặc không trả đủ thù lao lao động thì người lao động có thể theo pháp luật nộp đơn xin lệnh thanh toán lên tòa án nhân dân địa phương, tòa án nhân dân phải theo pháp luật ban hành lệnh thanh toán.\n" +
//            "Điều 31　Đơn vị sử dụng lao động phải thực hiện nghiêm ngặt tiêu chuẩn định mức lao động, không được ép buộc hoặc ép buộc dưới hình thức khác để người lao động làm thêm giờ. Đơn vị sử dụng lao động bố trí làm thêm giờ thì phải trả tiền làm thêm giờ cho người lao động theo quy định có liên quan của Nhà nước.\n" +
//            "Điều 32　Người lao động từ chối việc chỉ huy vi phạm quy định, ép buộc làm việc mạo hiểm của cán bộ quản lý đơn vị sử dụng lao động thì không bị coi là vi phạm hợp đồng lao động.\n" +
//            "Người lao động có quyền phê bình, tố giác và tố cáo đơn vị sử dụng lao động về điều kiện lao động gây nguy hại đến tính mạng, an toàn và sức khỏe.\n" +
//            "Điều 33　Việc đơn vị sử dụng lao động thay đổi tên gọi, người đại diện theo pháp luật, người phụ trách chính hoặc nhà đầu tư và các vấn đề khác không ảnh hưởng đến việc thực hiện hợp đồng lao động.\n" +
//            "Điều 34　Khi đơn vị sử dụng lao động sáp nhập hoặc chia tách, hợp đồng lao động ban đầu tiếp tục có hiệu lực, hợp đồng lao động do đơn vị sử dụng lao động kế thừa quyền và nghĩa vụ của nó tiếp tục thực hiện.\n" +
//            "Điều 35　Đơn vị sử dụng lao động và người lao động thỏa thuận nhất trí có thể thay đổi nội dung thỏa thuận trong hợp đồng lao động. Khi thay đổi hợp đồng lao động phải dùng hình thức văn bản.\n" +
//            "Văn bản hợp đồng lao động sau khi thay đổi do đơn vị sử dụng lao động và người lao động mỗi bên giữ một bản.\n" +
//            "Chương 4　Chấm dứt và kết thúc hợp đồng lao động\n" +
//            "Điều 36　Đơn vị sử dụng lao động và người lao động thỏa thuận nhất trí có thể chấm dứt hợp đồng lao động.\n" +
//            "Điều 37　Người lao động thông báo bằng văn bản cho đơn vị sử dụng lao động trước ba mươi ngày có thể chấm dứt hợp đồng lao động. Người lao động trong thời gian thử việc thông báo cho đơn vị sử dụng lao động trước ba ngày có thể chấm dứt hợp đồng lao động.\n" +
//            "Điều 38　Đơn vị sử dụng lao động có một trong các trường hợp sau đây thì người lao động có thể chấm dứt hợp đồng lao động:\n" +
//            "(1)Không cung cấp bảo hộ lao động hoặc điều kiện lao động theo thỏa thuận trong hợp đồng lao động;\n" +
//            "(2)Không trả thù lao lao động kịp thời và đầy đủ;\n" +
//            "(3)Không đóng bảo hiểm xã hội cho người lao động theo pháp luật;\n" +
//            "(4)Quy chế của đơn vị sử dụng lao động vi phạm quy định của pháp luật, quy định pháp luật, làm tổn hại đến quyền lợi của người lao động;\n" +
//            "(5)Do trường hợp quy định tại khoản 1 Điều 26 của Luật này khiến hợp đồng lao động vô hiệu;\n" +
//            "(6)Các trường hợp khác mà pháp luật, quy định hành chính quy định người lao động có thể chấm dứt hợp đồng lao động.\n" +
//            "Đơn vị sử dụng lao động dùng bạo lực, đe dọa hoặc hạn chế tự do thân thể bất hợp pháp để ép buộc người lao động làm việc, hoặc đơn vị sử dụng lao động chỉ huy vi phạm quy định, ép buộc làm việc mạo hiểm gây nguy hiểm đến an toàn thân thể của người lao động thì người lao động có thể chấm dứt hợp đồng lao động ngay lập tức mà không cần thông báo trước cho đơn vị sử dụng lao động.\n" +
//            "Điều 39　Người lao động có một trong các trường hợp sau đây thì đơn vị sử dụng lao động có thể chấm dứt hợp đồng lao động:\n" +
//            "(1)Trong thời gian thử việc bị chứng minh không đáp ứng điều kiện tuyển dụng;\n" +
//            "(2)Vi phạm nghiêm trọng quy chế của đơn vị sử dụng lao động;\n" +
//            "(3)Thiếu trách nhiệm nghiêm trọng, tư lợi gian lận, gây thiệt hại lớn cho đơn vị sử dụng lao động;\n" +
//            "(4)Người lao động đồng thời thiết lập quan hệ lao động với đơn vị sử dụng lao động khác, gây ảnh hưởng nghiêm trọng đến việc hoàn thành nhiệm vụ công việc của đơn vị này, hoặc sau khi đơn vị sử dụng lao động đề xuất mà vẫn từ chối sửa chữa;\n" +
//            "(5)Do trường hợp quy định tại khoản 1 Điều 26 của Luật này khiến hợp đồng lao động vô hiệu;\n" +
//            "(6)Bị truy cứu trách nhiệm hình sự theo pháp luật.\n" +
//            "Điều 40　Có một trong các trường hợp sau đây, đơn vị sử dụng lao động thông báo bằng văn bản cho người lao động trước ba mươi ngày hoặc trả thêm một tháng lương cho người lao động thì có thể chấm dứt hợp đồng lao động:\n" +
//            "(1)Người lao động bị bệnh hoặc bị thương không phải do công việc, sau khi hết thời gian điều trị theo quy định không thể làm công việc ban đầu, cũng không thể làm công việc do đơn vị sử dụng lao động bố trí khác;\n" +
//            "(2)Người lao động không đủ năng lực làm việc, sau khi đào tạo hoặc điều chỉnh vị trí công việc vẫn không đủ năng lực làm việc;\n" +
//            "(3)Tình hình khách quan làm căn cứ khi ký kết hợp đồng lao động thay đổi lớn, khiến hợp đồng lao động không thể thực hiện được, sau khi đơn vị sử dụng lao động và người lao động thương lượng mà không đạt được thỏa thuận về việc thay đổi nội dung hợp đồng lao động.\n" +
//            "Điều 41　Có một trong các trường hợp sau đây, cần cắt giảm từ hai mươi người trở lên hoặc cắt giảm dưới hai mươi người nhưng chiếm trên mười phần trăm tổng số công nhân viên chức của doanh nghiệp, đơn vị sử dụng lao động thông báo tình hình cho công đoàn hoặc toàn thể công nhân viên chức trước ba mươi ngày, lắng nghe ý kiến của công đoàn hoặc công nhân viên chức, sau đó phương án cắt giảm nhân sự được báo cáo lên cơ quan hành chính lao động thì có thể cắt giảm nhân sự:\n" +
//            "(1)Tiến hành tái cơ cấu theo quy định của Luật Phá sản doanh nghiệp;\n" +
//            "(2)Sản xuất kinh doanh gặp khó khăn nghiêm trọng;\n" +
//            "(3)Doanh nghiệp chuyển đổi sản xuất, đổi mới kỹ thuật lớn hoặc điều chỉnh phương thức kinh doanh, sau khi thay đổi hợp đồng lao động vẫn cần cắt giảm nhân sự;\n" +
//            "(4)Các trường hợp khác do tình hình kinh tế khách quan làm căn cứ khi ký kết hợp đồng lao động thay đổi lớn, khiến hợp đồng lao động không thể thực hiện được.\n" +
//            "Khi cắt giảm nhân sự, phải ưu tiên giữ lại những người sau:\n" +
//            "(1)Người đã ký kết hợp đồng lao động có thời hạn xác định dài hơn với đơn vị này;\n" +
//            "(2)Người đã ký kết hợp đồng lao động không xác định thời hạn với đơn vị này;\n" +
//            "(3)Gia đình không có người lao động khác, có người già hoặc trẻ vị thành niên cần nuôi dưỡng.\n" +
//            "Đơn vị sử dụng lao động cắt giảm nhân sự theo quy định tại khoản 1 Điều này, trong vòng sáu tháng tuyển dụng lại nhân sự thì phải thông báo cho những người bị cắt giảm, và trong điều kiện tương đương ưu tiên tuyển dụng lại những người bị cắt giảm.\n" +
//            "Điều 42　Người lao động có một trong các trường hợp sau đây thì đơn vị sử dụng lao động không được chấm dứt hợp đồng lao động theo quy định tại Điều 40, Điều 41 của Luật này:\n" +
//            "(1)Người lao động làm công việc tiếp xúc với nguy hại bệnh nghề nghiệp chưa được kiểm tra sức khỏe nghề nghiệp trước khi rời khỏi vị trí, hoặc người nghi mắc bệnh nghề nghiệp đang trong thời gian chẩn đoán hoặc theo dõi y tế;\n" +
//            "(2)Mắc bệnh nghề nghiệp hoặc bị thương do công việc tại đơn vị này và được xác nhận mất hoặc mất một phần khả năng lao động;\n" +
//            "(3)Bị bệnh hoặc bị thương không phải do công việc, trong thời gian điều trị theo quy định;\n" +
//            "(4)Lao động nữ trong thời kỳ mang thai, sinh con, nuôi con bằng sữa mẹ;\n" +
//            "(5)Làm việc liên tục đủ mười lăm năm tại đơn vị này và còn dưới năm năm đến tuổi nghỉ hưu theo luật định;\n" +
//            "(6)Các trường hợp khác theo quy định của pháp luật và quy định hành chính.\n" +
//            "Điều 43　Đơn vị sử dụng lao động đơn phương chấm dứt hợp đồng lao động phải thông báo lý do cho công đoàn trước. Đơn vị sử dụng lao động vi phạm quy định của pháp luật, quy định hành chính hoặc thỏa thuận trong hợp đồng lao động thì công đoàn có quyền yêu cầu đơn vị sử dụng lao động sửa chữa. Đơn vị sử dụng lao động phải nghiên cứu ý kiến của công đoàn và thông báo bằng văn bản kết quả xử lý cho công đoàn.\n" +
//            "Điều 44　Có một trong các trường hợp sau đây thì hợp đồng lao động kết thúc:\n" +
//            "(1)Hợp đồng lao động hết hạn;\n" +
//            "(2)Người lao động bắt đầu được hưởng chế độ bảo hiểm hưu trí cơ bản theo pháp luật;\n" +
//            "(3)Người lao động chết, hoặc bị tòa án nhân dân tuyên bố đã chết hoặc tuyên bố mất tích;\n" +
//            "(4)Đơn vị sử dụng lao động bị tuyên bố phá sản theo pháp luật;\n" +
//            "(5)Đơn vị sử dụng lao động bị thu hồi giấy phép kinh doanh, bị yêu cầu đóng cửa, bị thu hồi hoặc đơn vị sử dụng lao động quyết định giải thể trước thời hạn;\n" +
//            "(6)Các trường hợp khác theo quy định của pháp luật và quy định hành chính.\n" +
//            "Điều 45　Khi hợp đồng lao động hết hạn, nếu có một trong các trường hợp quy định tại Điều 42 của Luật này thì hợp đồng lao động phải được gia hạn đến khi trường hợp tương ứng chấm dứt. Tuy nhiên, việc kết thúc hợp đồng lao động của người lao động mất hoặc mất một phần khả năng lao động quy định tại khoản 2 Điều 42 của Luật này được thực hiện theo quy định có liên quan của Nhà nước về bảo hiểm tai nạn lao động.\n" +
//            "Điều 46　Có một trong các trường hợp sau đây thì đơn vị sử dụng lao động phải trả tiền bồi thường kinh tế cho người lao động:\n" +
//            "(1)Người lao động chấm dứt hợp đồng lao động theo quy định tại Điều 38 của Luật này;\n" +
//            "(2)Đơn vị sử dụng lao động đề xuất chấm dứt hợp đồng lao động với người lao động theo quy định tại Điều 36 của Luật này và thỏa thuận nhất trí với người lao động để chấm dứt hợp đồng lao động;\n" +
//            "(3)Đơn vị sử dụng lao động chấm dứt hợp đồng lao động theo quy định tại Điều 40 của Luật này;\n" +
//            "(4)Đơn vị sử dụng lao động chấm dứt hợp đồng lao động theo quy định tại khoản 1 Điều 41 của Luật này;\n" +
//            "(5)Trừ trường hợp đơn vị sử dụng lao động duy trì hoặc nâng cao điều kiện thỏa thuận trong hợp đồng lao động để gia hạn hợp đồng lao động mà người lao động không đồng ý gia hạn, việc kết thúc hợp đồng lao động có thời hạn xác định theo quy định tại khoản 1 Điều 44;\n" +
//            "(6)Việc kết thúc hợp đồng lao động theo quy định tại khoản 4, khoản 5 Điều 44 của Luật này;\n" +
//            "(7)Các trường hợp khác theo quy định của pháp luật và quy định hành chính.\n" +
//            "Điều 47　Tiền bồi thường kinh tế được trả cho người lao động theo số năm làm việc tại đơn vị này, mỗi năm đủ được trả theo tiêu chuẩn một tháng lương. Trên sáu tháng và dưới một năm được tính là một năm;Dưới sáu tháng thì trả cho người lao động tiền bồi thường kinh tế bằng nửa tháng lương.\n" +
//            "Nếu lương tháng của người lao động cao hơn ba lần mức lương tháng trung bình của công nhân viên chức trong khu vực năm trước do chính quyền nhân dân thành phố trực thuộc trung ương, thành phố cấp quận nơi đơn vị sử dụng lao động đặt trụ sở công bố thì tiêu chuẩn trả tiền bồi thường kinh tế cho người lao động được tính theo số tiền bằng ba lần mức lương tháng trung bình của công nhân viên chức, số năm trả tiền bồi thường kinh tế tối đa không vượt quá mười hai năm.\n" +
//            "Lương tháng quy định tại Điều này là mức lương trung bình của người lao động trong mười hai tháng trước khi hợp đồng lao động bị chấm dứt hoặc kết thúc.\n" +
//            "Điều 48　Đơn vị sử dụng lao động vi phạm quy định của Luật này để chấm dứt hoặc kết thúc hợp đồng lao động, người lao động yêu cầu tiếp tục thực hiện hợp đồng lao động thì đơn vị sử dụng lao động phải tiếp tục thực hiện;Người lao động không yêu cầu tiếp tục thực hiện hợp đồng lao động hoặc hợp đồng lao động đã không thể tiếp tục thực hiện thì đơn vị sử dụng lao động phải trả tiền bồi thường theo quy định tại Điều 87 của Luật này.\n" +
//            "Điều 49　Nhà nước áp dụng các biện pháp để thiết lập và hoàn thiện chế độ chuyển tiếp quan hệ bảo hiểm xã hội của người lao động giữa các khu vực.\n" +
//            "Điều 50　Đơn vị sử dụng lao động phải cấp giấy chứng nhận chấm dứt hoặc kết thúc hợp đồng lao động khi chấm dứt hoặc kết thúc hợp đồng lao động, và trong vòng mười lăm ngày làm thủ tục chuyển hồ sơ và quan hệ bảo hiểm xã hội cho người lao động.\n" +
//            "Người lao động phải thực hiện bàn giao công việc theo thỏa thuận của hai bên. Đơn vị sử dụng lao động theo quy định có liên quan của Luật này phải trả tiền bồi thường kinh tế cho người lao động thì trả khi hoàn tất bàn giao công việc.\n" +
//            "Đơn vị sử dụng lao động phải lưu giữ văn bản hợp đồng lao động đã chấm dứt hoặc kết thúc ít nhất hai năm để tra cứu.\n" +
//            "Chương 5　Quy định đặc biệt\n" +
//            "Mục 1　Hợp đồng tập thể\n" +
//            "Điều 51　Phía công nhân viên chức doanh nghiệp và đơn vị sử dụng lao động thông qua thương lượng bình đẳng có thể ký kết hợp đồng tập thể về các vấn đề như thù lao lao động, thời gian làm việc, nghỉ ngơi, an toàn vệ sinh lao động, bảo hiểm phúc lợi. Dự thảo hợp đồng tập thể phải được trình đại hội đại biểu công nhân viên chức hoặc toàn thể công nhân viên chức thảo luận và thông qua.\n" +
//            "Hợp đồng tập thể do công đoàn đại diện phía công nhân viên chức doanh nghiệp ký kết với đơn vị sử dụng lao động;Đơn vị sử dụng lao động chưa thành lập công đoàn thì do công đoàn cấp trên hướng dẫn đại diện do người lao động bầu ra ký kết với đơn vị sử dụng lao động.\n" +
//            "Điều 52　Phía công nhân viên chức doanh nghiệp và đơn vị sử dụng lao động có thể ký kết các hợp đồng tập thể chuyên đề như an toàn vệ sinh lao động, bảo vệ quyền lợi lao động nữ, cơ chế điều chỉnh tiền lương.\n" +
//            "Điều 53　Trong khu vực dưới cấp huyện, các ngành như xây dựng, khai thác mỏ, dịch vụ ăn uống có thể do công đoàn và đại diện phía doanh nghiệp ký kết hợp đồng tập thể theo ngành hoặc hợp đồng tập thể theo khu vực.\n" +
//            "Điều 54　Sau khi ký kết hợp đồng tập thể, phải báo cáo cơ quan hành chính lao động;Cơ quan hành chính lao động trong vòng mười lăm ngày kể từ ngày nhận được văn bản hợp đồng tập thể mà không có ý kiến phản đối thì hợp đồng tập thể có hiệu lực ngay.\n" +
//            "Hợp đồng tập thể được ký kết theo pháp luật có hiệu lực ràng buộc đối với đơn vị sử dụng lao động và người lao động. Hợp đồng tập thể theo ngành, theo khu vực có hiệu lực ràng buộc đối với đơn vị sử dụng lao động và người lao động trong ngành, khu vực đó tại địa phương.\n" +
//            "Điều 55　Các tiêu chuẩn như tiền lương và điều kiện lao động trong thỏa ước lao động tập thể không được thấp hơn mức tối thiểu do Chính quyền nhân dân địa phương quy định;Các tiêu chuẩn như tiền lương và điều kiện lao động trong hợp đồng lao động mà người sử dụng lao động ký kết với người lao động không được thấp hơn tiêu chuẩn quy định trong thỏa ước lao động tập thể。\n" +
//            "Điều 56　Người sử dụng lao động vi phạm thỏa ước lao động tập thể， xâm phạm quyền và lợi ích lao động của người lao động， công đoàn có thể yêu cầu người sử dụng lao động chịu trách nhiệm theo quy định của pháp luật;Khi phát sinh tranh chấp do thực hiện thỏa ước lao động tập thể， nếu thương lượng không giải quyết được， công đoàn có thể yêu cầu trọng tài hoặc khởi kiện theo quy định của pháp luật。\n" +
//            "Mục 2　Phái cử lao động\n" +
//            "Điều 57　Kinh doanh dịch vụ phái cử lao động phải đáp ứng các điều kiện sau đây：\n" +
//            "    （1）Vốn đăng ký không được ít hơn hai triệu Nhân dân tệ；\n" +
//            "    （2）Có địa điểm kinh doanh và cơ sở vật chất cố định phù hợp với việc triển khai hoạt động kinh doanh；\n" +
//            "    （3）Có chế độ quản lý phái cử lao động phù hợp với quy định của pháp luật và quy định hành chính；\n" +
//            "    （4）Các điều kiện khác theo quy định của pháp luật và quy định hành chính。\n" +
//            "    Kinh doanh dịch vụ phái cử lao động phải xin cấp phép hành chính từ cơ quan hành chính về lao động theo quy định của pháp luật； sau khi được cấp phép， phải làm thủ tục đăng ký công ty tương ứng theo quy định của pháp luật。 Nếu chưa được cấp phép， bất kỳ đơn vị hay cá nhân nào cũng không được kinh doanh dịch vụ phái cử lao động。\n" +
//            "Điều 58　Đơn vị phái cử lao động là người sử dụng lao động theo Luật này， phải thực hiện nghĩa vụ của người sử dụng lao động đối với người lao động。 Trong hợp đồng lao động mà đơn vị phái cử lao động ký kết với người lao động được phái cử， ngoài các nội dung quy định tại Điều 17 của Luật này， còn phải ghi rõ đơn vị sử dụng lao động của người lao động được phái cử cũng như thời hạn phái cử， vị trí công việc và các thông tin khác。\n" +
//            "Đơn vị phái cử lao động phải ký kết hợp đồng lao động có thời hạn cố định từ hai năm trở lên với người lao động được phái cử và trả tiền lương theo tháng;Trong thời gian người lao động được phái cử không có việc làm， đơn vị phái cử lao động phải trả lương cho họ theo tháng theo mức lương tối thiểu do Chính quyền nhân dân nơi đặt trụ sở quy định。\n" +
//            "Điều 59　Đơn vị phái cử lao động khi phái cử người lao động phải cùng đơn vị tiếp nhận lao động theo hình thức phái cử lao động(sau đây gọi là đơn vị sử dụng lao động)Ký kết thỏa thuận phái cử lao động。Thỏa thuận phái cử lao động phải quy định vị trí phái cử và số lượng nhân sự、thời hạn phái cử、số tiền và phương thức thanh toán tiền lương và phí bảo hiểm xã hội、cũng như trách nhiệm khi vi phạm thỏa thuận。\n" +
//            "Đơn vị sử dụng lao động phải xác định thời hạn phái cử với đơn vị phái cử lao động căn cứ vào nhu cầu thực tế của vị trí công việc， không được chia thời hạn sử dụng lao động liên tục thành nhiều thỏa thuận phái cử lao động ngắn hạn。\n" +
//            "Điều 60　Đơn vị phái cử lao động phải thông báo nội dung của thỏa thuận phái cử lao động cho người lao động được phái cử。\n" +
//            "Đơn vị phái cử lao động không được khấu trừ tiền lương mà đơn vị sử dụng lao động trả cho người lao động được phái cử theo thỏa thuận phái cử lao động。\n" +
//            "Đơn vị phái cử lao động và đơn vị sử dụng lao động không được thu phí của người lao động được phái cử。\n" +
//            "Điều 61　Khi đơn vị phái cử lao động phái cử người lao động sang khu vực khác， tiền lương và điều kiện lao động mà người lao động được phái cử được hưởng sẽ thực hiện theo tiêu chuẩn tại nơi đặt trụ sở của đơn vị sử dụng lao động。\n" +
//            "Điều 62　Đơn vị sử dụng lao động phải thực hiện các nghĩa vụ sau đây：\n" +
//            "(1)Thực hiện tiêu chuẩn lao động quốc gia， cung cấp điều kiện lao động và bảo hộ lao động tương ứng;\n" +
//            "(2)Thông báo yêu cầu công việc và tiền lương cho người lao động được phái cử;\n" +
//            "(3)Trả tiền làm thêm giờ， tiền thưởng hiệu suất， cung cấp phúc lợi liên quan đến vị trí công việc;\n" +
//            "(4)Đào tạo cần thiết cho vị trí công việc đối với người lao động được phái cử đang làm việc;\n" +
//            "(5)Đối với trường hợp sử dụng lao động liên tục， thực hiện cơ chế điều chỉnh tiền lương bình thường。\n" +
//            "Đơn vị sử dụng lao động không được phái cử lại người lao động được phái cử sang người sử dụng lao động khác。\n" +
//            "Điều 63　Người lao động được phái cử được hưởng quyền cùng công việc， cùng tiền lương như người lao động của đơn vị sử dụng lao động。 Đơn vị sử dụng lao động phải áp dụng cùng một phương pháp phân phối tiền lương đối với người lao động được phái cử và người lao động ở vị trí cùng loại trong đơn vị theo nguyên tắc cùng công việc， cùng tiền lương。 Nếu đơn vị sử dụng lao động không có người lao động ở vị trí cùng loại， tiền lương được xác định tham chiếu theo tiền lương của người lao động ở vị trí tương tự hoặc gần tương tự tại nơi đặt trụ sở của đơn vị sử dụng lao động。\n" +
//            "Tiền lương trả cho người lao động được phái cử được ghi rõ hoặc thỏa thuận trong hợp đồng lao động mà đơn vị phái cử lao động ký kết với người lao động được phái cử và trong thỏa thuận phái cử lao động ký kết với đơn vị sử dụng lao động phải phù hợp với quy định tại khoản trên。\n" +
//            "Điều 64　Người lao động được phái cử có quyền tham gia hoặc tổ chức công đoàn theo quy định của pháp luật tại đơn vị phái cử lao động hoặc đơn vị sử dụng lao động để bảo vệ quyền và lợi ích hợp pháp của mình。\n" +
//            "Điều 65　Người lao động được phái cử có thể chấm dứt hợp đồng lao động với đơn vị phái cử lao động theo quy định tại Điều 36 và Điều 38 của Luật này。\n" +
//            "Người lao động được phái cử thuộc các trường hợp quy định tại Điều 39 và khoản 1， khoản 2 Điều 40 của Luật này thì đơn vị sử dụng lao động có thể trả người lao động về đơn vị phái cử lao động， và đơn vị phái cử lao động có thể chấm dứt hợp đồng lao động với người lao động theo các quy định liên quan của Luật này。\n" +
//            "Điều 66　Sử dụng lao động theo hợp đồng lao động là hình thức sử dụng lao động cơ bản của doanh nghiệp nước ta。 Sử dụng lao động theo hình thức phái cử lao động là hình thức bổ sung， chỉ được thực hiện ở các vị trí công việc mang tính tạm thời， phụ trợ hoặc thay thế。\n" +
//            "    Vị trí công việc tạm thời quy định tại khoản trên là vị trí tồn tại không quá sáu tháng； vị trí công việc phụ trợ là vị trí không thuộc hoạt động kinh doanh chính， cung cấp dịch vụ cho vị trí thuộc hoạt động kinh doanh chính； vị trí công việc thay thế là vị trí mà trong một khoảng thời gian nhất định khi người lao động của đơn vị sử dụng lao động không thể làm việc do học tập trung， nghỉ phép và các lý do khác， có thể do người lao động khác thay thế làm việc。\n" +
//            "    Đơn vị sử dụng lao động phải kiểm soát chặt chẽ số lượng lao động phái cử， không được vượt quá một tỷ lệ nhất định trong tổng số lao động sử dụng， tỷ lệ cụ thể do cơ quan hành chính về lao động thuộc Quốc vụ viện quy định。\n" +
//            "Điều 67　Người sử dụng lao động không được thành lập đơn vị phái cử lao động để phái cử người lao động cho đơn vị mình hoặc đơn vị trực thuộc。\n" +
//            "Mục 3　Sử dụng lao động bán thời gian\n" +
//            "Điều 68　Sử dụng lao động bán thời gian là hình thức sử dụng lao động trong đó tiền lương được tính chủ yếu theo giờ， người lao động làm việc tại cùng một người sử dụng lao động với thời gian làm việc bình quân mỗi ngày thường không quá bốn giờ và tổng thời gian làm việc mỗi tuần không quá hai mươi bốn giờ。\n" +
//            "Điều 69　Các bên trong quan hệ sử dụng lao động bán thời gian có thể giao kết thỏa thuận bằng lời nói。\n" +
//            "Người lao động làm việc bán thời gian có thể ký kết hợp đồng lao động với một hoặc nhiều người sử dụng lao động;Tuy nhiên， hợp đồng lao động ký kết sau không được ảnh hưởng đến việc thực hiện hợp đồng lao động đã ký kết trước。\n" +
//            "Điều 70　Các bên trong quan hệ sử dụng lao động bán thời gian không được thỏa thuận thời gian thử việc。\n" +
//            "Điều 71　Bất kỳ bên nào trong quan hệ sử dụng lao động bán thời gian cũng có thể thông báo cho bên kia chấm dứt việc sử dụng lao động bất cứ lúc nào。 Khi chấm dứt việc sử dụng lao động， người sử dụng lao động không phải trả tiền bồi thường kinh tế cho người lao động。\n" +
//            "Điều 72　Mức tính lương theo giờ đối với lao động bán thời gian không được thấp hơn mức lương tối thiểu theo giờ do Chính quyền nhân dân nơi đặt trụ sở của người sử dụng lao động quy định。\n" +
//            "Chu kỳ quyết toán và trả tiền lương đối với lao động bán thời gian dài nhất không được quá mười lăm ngày。\n" +
//            "Chương 6　Giám sát kiểm tra\n" +
//            "Điều 73　Cơ quan hành chính về lao động thuộc Quốc vụ viện chịu trách nhiệm giám sát， quản lý việc thực hiện chế độ hợp đồng lao động trên phạm vi cả nước。\n" +
//            "Cơ quan hành chính về lao động thuộc Chính quyền nhân dân địa phương cấp huyện trở lên chịu trách nhiệm giám sát， quản lý việc thực hiện chế độ hợp đồng lao động trong khu vực hành chính của mình。\n" +
//            "Trong công tác giám sát， quản lý việc thực hiện chế độ hợp đồng lao động， cơ quan hành chính về lao động thuộc Chính quyền nhân dân các cấp từ cấp huyện trở lên phải lắng nghe ý kiến của công đoàn， đại diện phía doanh nghiệp và các cơ quan chủ quản ngành có liên quan。\n" +
//            "Điều 74　Cơ quan hành chính về lao động thuộc Chính quyền nhân dân địa phương cấp huyện trở lên thực hiện giám sát， kiểm tra theo quy định của pháp luật đối với các trường hợp thực hiện chế độ hợp đồng lao động sau đây：\n" +
//            "(1)Tình hình người sử dụng lao động ban hành và thực hiện nội quy， quy chế liên quan trực tiếp đến quyền lợi thiết thân của người lao động;\n" +
//            "(2)Tình hình người sử dụng lao động ký kết và chấm dứt hợp đồng lao động với người lao động;\n" +
//            "(3)Tình hình đơn vị phái cử lao động và đơn vị sử dụng lao động tuân thủ các quy định liên quan đến phái cử lao động;\n" +
//            "(4)Tình hình người sử dụng lao động tuân thủ các quy định của Nhà nước về thời gian làm việc và nghỉ ngơi， nghỉ phép của người lao động;\n" +
//            "(5)Tình hình người sử dụng lao động trả tiền lương theo thỏa thuận trong hợp đồng lao động và thực hiện mức lương tối thiểu;\n" +
//            "(6)Tình hình người sử dụng lao động tham gia các loại bảo hiểm xã hội và đóng phí bảo hiểm xã hội;\n" +
//            "(7)Các vấn đề thanh tra lao động khác theo quy định của pháp luật và quy định pháp luật。\n" +
//            "Điều 75　Khi cơ quan hành chính về lao động thuộc Chính quyền nhân dân địa phương cấp huyện trở lên thực hiện giám sát， kiểm tra， có quyền xem xét các tài liệu liên quan đến hợp đồng lao động， thỏa ước lao động tập thể， có quyền kiểm tra thực địa tại nơi làm việc， và người sử dụng lao động cũng như người lao động đều phải cung cấp trung thực các thông tin và tài liệu liên quan。\n" +
//            "Nhân viên của cơ quan hành chính về lao động khi thực hiện giám sát， kiểm tra phải xuất trình giấy tờ， thực hiện chức quyền theo quy định của pháp luật và chấp pháp một cách văn minh。\n" +
//            "Điều 76　Các cơ quan chủ quản có liên quan như xây dựng， y tế， giám sát quản lý an toàn sản xuất thuộc Chính quyền nhân dân cấp huyện trở lên， trong phạm vi chức trách của mình， thực hiện giám sát， quản lý tình hình người sử dụng lao động thực hiện chế độ hợp đồng lao động。\n" +
//            "Điều 77　Người lao động khi quyền và lợi ích hợp pháp bị xâm phạm có quyền yêu cầu cơ quan có liên quan xử lý theo quy định của pháp luật， hoặc yêu cầu trọng tài， khởi kiện theo quy định của pháp luật。\n" +
//            "Điều 78　Công đoàn bảo vệ quyền và lợi ích hợp pháp của người lao động theo quy định của pháp luật， giám sát tình hình người sử dụng lao động thực hiện hợp đồng lao động và thỏa ước lao động tập thể。 Người sử dụng lao động vi phạm pháp luật lao động， quy định pháp luật， hợp đồng lao động và thỏa ước lao động tập thể thì công đoàn có quyền đưa ra ý kiến hoặc yêu cầu sửa chữa;Khi người lao động yêu cầu trọng tài hoặc khởi kiện， công đoàn sẽ hỗ trợ và giúp đỡ theo quy định của pháp luật。\n" +
//            "Điều 79　Bất kỳ tổ chức hoặc cá nhân nào cũng có quyền tố cáo hành vi vi phạm Luật này， cơ quan hành chính về lao động thuộc Chính quyền nhân dân cấp huyện trở lên phải kịp thời xác minh， xử lý và khen thưởng những người tố cáo có công。\n" +
//            "Chương 7　Trách nhiệm pháp lý\n" +
//            "Điều 80　Nội quy， quy chế của người sử dụng lao động liên quan trực tiếp đến quyền lợi thiết thân của người lao động mà vi phạm quy định của pháp luật， quy định pháp luật thì cơ quan hành chính về lao động buộc sửa đổi và cảnh cáo;Nếu gây thiệt hại cho người lao động thì phải chịu trách nhiệm bồi thường.\n" +
//            "Điều 81　Văn bản hợp đồng lao động do người sử dụng lao động cung cấp không ghi rõ các điều khoản bắt buộc của hợp đồng lao động theo quy định của Luật này， hoặc người sử dụng lao động không giao văn bản hợp đồng lao động cho người lao động， thì cơ quan hành chính về lao động buộc sửa đổi;Nếu gây thiệt hại cho người lao động thì phải chịu trách nhiệm bồi thường.\n" +
//            "Điều 82　Người sử dụng lao động kể từ ngày bắt đầu sử dụng lao động， quá một tháng nhưng chưa đầy một năm mà chưa ký kết hợp đồng lao động bằng văn bản với người lao động， phải trả cho người lao động mỗi tháng gấp đôi tiền lương。\n" +
//            "Người sử dụng lao động vi phạm quy định của Luật này， không ký kết hợp đồng lao động không xác định thời hạn với người lao động， thì kể từ ngày phải ký kết hợp đồng lao động không xác định thời hạn， phải trả cho người lao động mỗi tháng gấp đôi tiền lương。\n" +
//            "Điều 83　Người sử dụng lao động vi phạm quy định của Luật này khi thỏa thuận thời gian thử việc với người lao động thì cơ quan hành chính về lao động buộc sửa đổi;Đối với thời gian thử việc đã thực hiện theo thỏa thuận trái pháp luật， người sử dụng lao động phải trả tiền bồi thường cho người lao động theo tiêu chuẩn là tiền lương tháng khi hết thời gian thử việc， tính theo thời gian đã thực hiện vượt quá thời gian thử việc theo quy định của pháp luật。\n" +
//            "Điều 84　Người sử dụng lao động vi phạm quy định của Luật này， giữ chứng minh nhân dân và các giấy tờ khác của người lao động， thì cơ quan hành chính về lao động buộc trả lại cho chính người lao động trong thời hạn quy định và xử phạt theo quy định của pháp luật có liên quan。\n" +
//            "Người sử dụng lao động vi phạm quy định của Luật này， thu tiền， tài sản của người lao động dưới danh nghĩa bảo lãnh hoặc danh nghĩa khác， thì cơ quan hành chính về lao động buộc trả lại cho chính người lao động trong thời hạn quy định và xử phạt với mức từ năm trăm Nhân dân tệ đến hai nghìn Nhân dân tệ cho mỗi người;Nếu gây thiệt hại cho người lao động thì phải chịu trách nhiệm bồi thường.\n" +
//            "Người lao động chấm dứt hoặc kết thúc hợp đồng lao động theo quy định của pháp luật mà người sử dụng lao động giữ hồ sơ hoặc các vật phẩm khác của người lao động thì xử phạt theo quy định tại khoản trên。\n" +
//            "Điều 85　Người sử dụng lao động thuộc một trong các trường hợp sau đây thì cơ quan hành chính về lao động buộc trả tiền lương， tiền làm thêm giờ hoặc tiền bồi thường kinh tế trong thời hạn quy định;Tiền lương thấp hơn mức lương tối thiểu của địa phương thì phải trả phần chênh lệch;Quá hạn không trả thì buộc người sử dụng lao động trả thêm tiền bồi thường cho người lao động theo mức từ 50% đến 100% số tiền phải trả：\n" +
//            "(1)Không trả tiền lương cho người lao động kịp thời và đầy đủ theo thỏa thuận trong hợp đồng lao động hoặc theo quy định của Nhà nước;\n" +
//            "(2)Trả lương cho người lao động thấp hơn mức lương tối thiểu của địa phương;\n" +
//            "(3)Bố trí làm thêm giờ mà không trả tiền làm thêm giờ;\n" +
//            "(4)Chấm dứt hoặc kết thúc hợp đồng lao động mà không trả tiền bồi thường kinh tế cho người lao động theo quy định của Luật này。\n" +
//            "Điều 86　Hợp đồng lao động bị xác nhận vô hiệu theo quy định tại Điều 26 của Luật này， gây thiệt hại cho bên kia， thì bên có lỗi phải chịu trách nhiệm bồi thường。\n" +
//            "Điều 87　Người sử dụng lao động vi phạm quy định của Luật này khi chấm dứt hoặc kết thúc hợp đồng lao động phải trả tiền bồi thường cho người lao động gấp đôi mức tiền bồi thường kinh tế quy định tại Điều 47 của Luật này。\n" +
//            "Điều 88　Người sử dụng lao động thuộc một trong các trường hợp sau đây sẽ bị xử phạt hành chính theo quy định của pháp luật;Nếu cấu thành tội phạm， sẽ bị truy cứu trách nhiệm hình sự theo quy định của pháp luật;Gây thiệt hại cho người lao động thì phải chịu trách nhiệm bồi thường：\n" +
//            "(1)Dùng bạo lực， đe dọa hoặc hạn chế trái pháp luật quyền tự do thân thể để ép buộc lao động;\n" +
//            "(2)Chỉ huy vi phạm quy định hoặc ép buộc làm việc mạo hiểm gây nguy hiểm đến an toàn tính mạng của người lao động;\n" +
//            "(3)Xúc phạm， trừng phạt thể xác， đánh đập， khám xét trái phép hoặc giam giữ trái phép người lao động;\n" +
//            "(4)Điều kiện lao động tồi tệ， ô nhiễm môi trường nghiêm trọng， gây thiệt hại nghiêm trọng đến sức khỏe thể chất và tinh thần của người lao động。\n" +
//            "Điều 89　Người sử dụng lao động vi phạm quy định của Luật này， không cấp văn bản chứng minh việc chấm dứt hoặc kết thúc hợp đồng lao động cho người lao động， thì cơ quan hành chính về lao động buộc sửa đổi;Nếu gây thiệt hại cho người lao động thì phải chịu trách nhiệm bồi thường.\n" +
//            "Điều 90　Người lao động vi phạm quy định của Luật này khi chấm dứt hợp đồng lao động， hoặc vi phạm nghĩa vụ bảo mật hoặc hạn chế cạnh tranh đã thỏa thuận trong hợp đồng lao động， gây thiệt hại cho người sử dụng lao động， thì phải chịu trách nhiệm bồi thường。\n" +
//            "Điều 91　Người sử dụng lao động tuyển dụng người lao động chưa chấm dứt hoặc kết thúc hợp đồng lao động với người sử dụng lao động khác， gây thiệt hại cho người sử dụng lao động khác， thì phải chịu trách nhiệm bồi thường liên đới。\n" +
//            "Điều 92　Vi phạm quy định của Luật này， kinh doanh dịch vụ phái cử lao động khi chưa được cấp phép， thì cơ quan hành chính về lao động buộc chấm dứt hành vi vi phạm pháp luật， tịch thu thu nhập bất hợp pháp và xử phạt từ một đến năm lần thu nhập bất hợp pháp； nếu không có thu nhập bất hợp pháp thì có thể xử phạt dưới năm mươi nghìn Nhân dân tệ。\n" +
//            "    Đơn vị phái cử lao động， đơn vị sử dụng lao động vi phạm các quy định của Luật này về phái cử lao động thì cơ quan hành chính về lao động buộc sửa đổi trong thời hạn quy định； quá hạn không sửa đổi thì xử phạt với mức từ năm nghìn Nhân dân tệ đến mười nghìn Nhân dân tệ cho mỗi người， đối với đơn vị phái cử lao động thì thu hồi giấy phép kinh doanh dịch vụ phái cử lao động。 Đơn vị sử dụng lao động gây thiệt hại cho người lao động được phái cử thì đơn vị phái cử lao động và đơn vị sử dụng lao động chịu trách nhiệm bồi thường liên đới。\n" +
//            "Điều 93　Đối với hành vi vi phạm pháp luật và phạm tội của người sử dụng lao động không có tư cách kinh doanh hợp pháp， sẽ bị truy cứu trách nhiệm pháp lý theo quy định của pháp luật;Người lao động đã làm việc thì đơn vị đó hoặc người góp vốn của đơn vị đó phải trả tiền lương， tiền bồi thường kinh tế， tiền bồi thường cho người lao động theo các quy định liên quan của Luật này;Nếu gây thiệt hại cho người lao động thì phải chịu trách nhiệm bồi thường.\n" +
//            "Điều 94　Cá nhân nhận thầu kinh doanh vi phạm quy định của Luật này khi tuyển dụng người lao động， gây thiệt hại cho người lao động， thì tổ chức giao thầu và cá nhân nhận thầu kinh doanh chịu trách nhiệm bồi thường liên đới。\n" +
//            "Điều 95　Cơ quan hành chính về lao động và các cơ quan chủ quản có liên quan khác cùng nhân viên của họ lơ là chức trách， không thực hiện trách nhiệm theo quy định của pháp luật， hoặc thực hiện chức quyền trái pháp luật， gây thiệt hại cho người lao động hoặc người sử dụng lao động， thì phải chịu trách nhiệm bồi thường;Đối với cán bộ quản lý trực tiếp chịu trách nhiệm và các nhân viên chịu trách nhiệm trực tiếp khác， sẽ bị xử lý kỷ luật hành chính theo quy định của pháp luật;Nếu cấu thành tội phạm， sẽ bị truy cứu trách nhiệm hình sự theo quy định của pháp luật。\n" +
//            "Chương 8　Phụ tắc\n" +
//            "Điều 96　Đơn vị sự nghiệp ký kết， thực hiện， thay đổi， chấm dứt hoặc kết thúc hợp đồng lao động với nhân viên thực hiện chế độ tuyển dụng， nếu pháp luật， quy định hành chính hoặc Quốc vụ viện có quy định khác thì thực hiện theo quy định đó;Trường hợp chưa có quy định thì thực hiện theo các quy định liên quan của Luật này。\n" +
//            "Điều 97　Hợp đồng lao động đã được ký kết hợp pháp trước khi Luật này có hiệu lực thi hành và còn hiệu lực vào ngày Luật này có hiệu lực thi hành thì tiếp tục thực hiện;Số lần ký kết liên tiếp hợp đồng lao động có thời hạn cố định quy định tại khoản 2， điểm 3 Điều 14 của Luật này được tính bắt đầu từ lần ký tiếp hợp đồng lao động có thời hạn cố định sau khi Luật này có hiệu lực thi hành。\n" +
//            "Quan hệ lao động đã được thiết lập trước khi Luật này có hiệu lực thi hành mà chưa ký kết hợp đồng lao động bằng văn bản thì phải ký kết trong vòng một tháng kể từ ngày Luật này có hiệu lực thi hành。\n" +
//            "Hợp đồng lao động còn hiệu lực vào ngày Luật này có hiệu lực thi hành mà bị chấm dứt hoặc kết thúc sau khi Luật này có hiệu lực thi hành， nếu theo quy định tại Điều 46 của Luật này phải trả tiền bồi thường kinh tế thì số năm tính tiền bồi thường kinh tế được tính kể từ ngày Luật này có hiệu lực thi hành;Trước khi Luật này có hiệu lực thi hành， theo các quy định hiện hành lúc đó， người sử dụng lao động phải trả tiền bồi thường kinh tế cho người lao động thì thực hiện theo các quy định hiện hành lúc đó。\n" +
//            "Điều 98　Luật này có hiệu lực thi hành kể từ ngày 1 tháng 1 năm 2008。\n" +
//            "\n";
//
//
//}
