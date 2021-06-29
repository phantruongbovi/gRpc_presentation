package blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client for Blog");
        BlogClient main = new BlogClient();
        main.run();
    }

    private void run(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        createBlog(channel);
    }

    private void createBlog(ManagedChannel channel){
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Truong")
                .setTitle("Daily life")
                .setContent("day1: ... , day2: ...")
                .build();

        CreateBlogResponse blogResponse  = blogClient.createBlog(
                CreateBlogRequest.newBuilder().setBlog(blog).build()
        );
        System.out.println("Received create blog response!");
        System.out.println(blogResponse.toString());
    }
}
