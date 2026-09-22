package com.vircarmen.botica.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vircarmen.botica.config.SunatProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SunatXmlSigner {
    private final SunatProperties properties;

    public String firmar(String xml) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] password = properties.getCertificatePassword().toCharArray();
        try (InputStream input = certificado()) {
            keyStore.load(input, password);
        }
        String alias = Collections.list(keyStore.aliases()).stream()
                .filter(a -> {
                    try { return keyStore.isKeyEntry(a); } catch (Exception ex) { return false; }
                }).findFirst().orElseThrow(() -> new IllegalStateException("El PKCS12 no contiene una clave privada."));
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        Document document = dbf.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        NodeList contents = document.getElementsByTagNameNS(
                "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2", "ExtensionContent");
        Node parent = contents.getLength() == 0 ? document.getDocumentElement() : contents.item(0);

        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        Transform enveloped = factory.newTransform(Transform.ENVELOPED, (javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
        Reference reference = factory.newReference("", factory.newDigestMethod(DigestMethod.SHA256, null),
                List.of(enveloped), null, null);
        SignedInfo signedInfo = factory.newSignedInfo(
                factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                        (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                factory.newSignatureMethod(SignatureMethod.RSA_SHA256, null), List.of(reference));
        KeyInfoFactory kif = factory.getKeyInfoFactory();
        X509Data x509Data = kif.newX509Data(List.of(certificate.getSubjectX500Principal().getName(), certificate));
        KeyInfo keyInfo = kif.newKeyInfo(List.of(x509Data));
        DOMSignContext context = new DOMSignContext(privateKey, parent);
        context.setDefaultNamespacePrefix("ds");
        XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo, null, "SignatureSP", null);
        signature.sign(context);

        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private InputStream certificado() throws Exception {
        if (properties.getCertificateBase64() != null && !properties.getCertificateBase64().isBlank()) {
            return new ByteArrayInputStream(Base64.getDecoder().decode(properties.getCertificateBase64().trim()));
        }
        return Files.newInputStream(Path.of(properties.getCertificatePath()));
    }
}
