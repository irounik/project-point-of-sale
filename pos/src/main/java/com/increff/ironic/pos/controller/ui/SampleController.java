package com.increff.ironic.pos.controller.ui;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class SampleController {

    @RequestMapping(value = "/sample/{fileName:.+}", method = RequestMethod.GET)
    public void getFile(@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException {
        // get your file as InputStream
        response.setContentType("text/csv");
        response.addHeader("Content-disposition:", "attachment; filename=" + fileName);
        String fileClasspath = "/com/increff/ironic/pos/samples/" + fileName;

        // copy it to response's OutputStream
        try (InputStream is = SampleController.class.getResourceAsStream(fileClasspath)) {
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
