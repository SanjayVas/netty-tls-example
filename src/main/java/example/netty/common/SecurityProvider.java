package example.netty.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public abstract class SecurityProvider {
  private SecurityProvider() {}

  private static final String CERTIFICATE_TYPE = "X.509";
  private static final List<String> KEY_ALGORITHMS =
      Collections.unmodifiableList(Arrays.asList("EdDSA", "EC", "RSA"));
  private static final Provider jceProvider;

  static {
    jceProvider = new BouncyCastleProvider();
    Security.addProvider(jceProvider);
  }

  /** Reads a private key from a PKCS#8-encoded PEM file. */
  public static PrivateKey readPrivateKey(File pemFile)
      throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
    KeySpec keySpec = new PKCS8EncodedKeySpec(readPemFile(pemFile).getContent());
    for (String keyAlgorithm : KEY_ALGORITHMS) {
      try {
        return KeyFactory.getInstance(keyAlgorithm, jceProvider).generatePrivate(keySpec);
      } catch (InvalidKeySpecException e) {
        // No-op.
      }
    }

    throw new InvalidKeySpecException(
        "None of the following key algorithms worked: " + KEY_ALGORITHMS.toString());
  }

  /** Reads an X.509 certificate from a PEM file. */
  public static X509Certificate readCertificate(File pemFile)
      throws CertificateException, IOException {
    try (FileInputStream inputStream = new FileInputStream(pemFile)) {
      return (X509Certificate)
          CertificateFactory.getInstance(CERTIFICATE_TYPE, jceProvider)
              .generateCertificate(inputStream);
    }
  }

  /** Reads an X.509 certificate collection from a PEM file. */
  @SuppressWarnings(value = "unchecked") // Safe as collection is immutable.
  public static Collection<X509Certificate> readCertificateCollection(File pemFile)
      throws CertificateException, IOException {
    try (FileInputStream inputStream = new FileInputStream(pemFile)) {
      return (Collection<X509Certificate>)
          Collections.unmodifiableCollection(
              CertificateFactory.getInstance(CERTIFICATE_TYPE, jceProvider)
                  .generateCertificates(inputStream));
    }
  }

  /** Reads the first {@link PemObject} from a PEM file. */
  private static PemObject readPemFile(File pemFile) throws IOException {
    try (FileInputStream inputStream = new FileInputStream(pemFile)) {
      return new PemReader(new InputStreamReader(inputStream)).readPemObject();
    }
  }
}
