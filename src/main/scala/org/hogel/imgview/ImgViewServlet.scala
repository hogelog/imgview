package org.hogel.imgview

import org.scalatra._
import scalate.ScalateSupport
import java.io.File

class ImgViewServlet extends ScalatraServlet with ScalateSupport {
  val HOME_DIR = System.getenv.get("HOME")

  get("/") {
    redirect("/nav")
  }

  get("/nav*") {
    val rawQuery = params("splat")
    val query =
      if (rawQuery.last == '/')
        rawQuery.substring(0, rawQuery.length - 1)
      else
        rawQuery
    val path = HOME_DIR + query
    val file:File = new File(path)
    val content =
      if (file.isDirectory) {
        val files = file.listFiles()
        <h1>Index of {file}</h1>
        <ul>{
          for (f <- files) yield {
            val name = f.getName
            if (name.head != '.')
              <li>{
                <a href={url("/nav" + query + '/' + name)}>{name}</a>
              }</li>
          }
        }</ul>
      } else if (file.exists) {
        val dir = file.getParentFile
        val files: Array[File] = dir.listFiles
        val pos:Int = refArrayOps(files).indexOf(file)
        <img src={url("/view" + query)} width="740px" usemap="#map" />
        <map name="map">
          {
            if (pos > 0)
              <area shape="rect" coords="0,0,350,900" href={files(pos - 1).getName} />
          }
          {
            if (pos + 1 < files.length)
              <area shape="rect" coords="350,0,740,900" href={files(pos+1).getName} />
          }
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
