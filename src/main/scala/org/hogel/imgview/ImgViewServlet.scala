package org.hogel.imgview

import org.scalatra._
import scalate.ScalateSupport
import org.apache.commons.io.{FilenameUtils, FileUtils}
import java.io.File
import xml.{Node, Elem}
import java.util
import org.apache.commons.lang3.ArrayUtils

class ImgViewServlet extends ScalatraServlet with ScalateSupport {
  val HOME_DIR = System.getenv.get("HOME")

  get("/") {
    redirect("/nav")
  }

  get("/nav*") {
    val query = params("splat")
    val path = HOME_DIR + query
    val file:File = new File(path)
    val content =
      if (file.isDirectory) {
        val files = file.listFiles()
        <h1>Index of {file}</h1>
          <ul>
            {
            for (f <- files) yield
              <li>
                {
                val name = f.getName
                <a href={url("/nav" + query + '/' + name)}>{name}</a>
                }
              </li>
            }
          </ul>
      } else if (file.exists) {
        val dir = file.getParentFile
        val files: Array[File] = dir.listFiles
        val pos:Int = refArrayOps(files).indexOf(file)
        val prev:File = (if (pos > 0) files(pos - 1) else file)
        val next:File = (if (pos + 1 < files.length) files(pos + 1) else file)
        <img src={url("/view" + query)} width="740px" usemap="#map" />
        <map name="map">
          <area shape="rect" coords="0,0,350,900" href={prev.getName} />
          <area shape="rect" coords="350,0,740,900" href={next.getName} />
        </map>
        <p><a href="..">../</a></p>
      } else {
        println(file + " does not exists!")
        <p>not found: {file}</p>
      }

    <html>
      <head>
        <title>{path}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
        </head>
      <body>
        {content}
      </body>
    </html>
  }

  get("/view*") {
    val path = HOME_DIR + params("splat")
    val file:File = new File(path)
    contentType = "image/jpeg"
    file
  }

  notFound {
    // remove content type in case it was set through an action
    contentType = null 
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound() 
  }
}
