package dev.bradburylabs.homedrive.api.internal;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import dev.bradburylabs.homedrive.model.object.GetObjectResponse;
import dev.bradburylabs.homedrive.model.object.ListObjectsResponse;
import dev.bradburylabs.homedrive.model.object.ObjectModel;
import dev.bradburylabs.homedrive.service.ObjectService;
import dev.bradburylabs.homedrive.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/object")
@RequiredArgsConstructor
public class ObjectController {
    private final UserService userService;
    private final ObjectService objectService;

    @GetMapping
    public ListObjectsResponse listObjects(@RequestParam(value = "parentId", required = false) String parentId,
            @RequestParam(value = "continuationToken", required = false) String continuationToken, Pageable pageable, Authentication authentication) {
        String userId = userService.readUserByUsername(authentication.getName()).id();

        return objectService.listObjects(userId, parentId, continuationToken, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StreamingResponseBody> getObject(@PathVariable String id, Authentication authentication) {
        String userId = userService.readUserByUsername(authentication.getName()).id();
        GetObjectResponse response = objectService.getObject(id, userId, true);

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = response.inputStream()) {
                inputStream.transferTo(outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        ObjectModel objectModel = response.objectModel();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentLength(objectModel.contentLength());
        headers.setContentDisposition(ContentDisposition.attachment().filename(objectModel.name()).build());

        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

    @GetMapping("/{id}/details")
    public ObjectModel getObjectDetails(@PathVariable String id, Authentication authentication) {
        String userId = userService.readUserByUsername(authentication.getName()).id();

        return objectService.getObject(id, userId, false).objectModel();
    }
}
