package example.netty.server;

import example.netty.common.SecurityProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Server", mixinStandardHelpOptions = true, description = "Netty server")
public class Server implements Runnable {

  @Option(names = "--port", description = "Server port", defaultValue = "50440")
  private int port;

  @Option(
      names = {"--certificate", "--cert"},
      description = "X.509 certificate in PEM format.",
      required = true)
  private File certificateFile;

  @Option(
      names = "--private-key",
      description = "PKCS #8 private key in PEM format.",
      required = true)
  private File privateKeyFile;

  @Override
  public void run() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
      ServerBootstrap b = new ServerBootstrap();

      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(new ServerInitializer(buildSslContext()));

      // Bind socket and start server.
      ChannelFuture f = b.bind(port).sync();

      // Block until server shutdown.
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      } else {
        throw new RuntimeException(e);
      }
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

  private SslContext buildSslContext()
      throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, CertificateException {
    return SslContextBuilder.forServer(
            SecurityProvider.readPrivateKey(privateKeyFile),
            SecurityProvider.readCertificate(certificateFile))
        .protocols("TLSv1.3", "TLSv1.2")
        .build();
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Server()).execute(args);
    System.exit(exitCode);
  }

  private static class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslContext;

    public ServerInitializer(SslContext sslContext) {
      this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
      ChannelPipeline pipeline = channel.pipeline();
      pipeline.addLast(sslContext.newHandler(channel.alloc()));
    }
  }
}
