package cinematch;

import io.javalin.Javalin;

public final class Greeting {

    private Greeting() { }

    public static void register(Javalin app) {
        app.get("/api/v1/greeting", ctx -> ctx.contentType("text/html; charset=utf-8").result(PAGE));
    }

    private static final String PAGE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>CineMatch API</title>
              <style>
                * { box-sizing: border-box; }
                body { margin: 0; font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
                       background: #f4f5f7; color: #1f2933; line-height: 1.6; }
                header { background: #1d2a44; color: #fff; padding: 40px 20px; text-align: center; }
                header h1 { margin: 0 0 8px; font-size: 2rem; }
                header p { margin: 0 auto; max-width: 680px; opacity: .9; }
                main { max-width: 960px; margin: 0 auto; padding: 24px 16px 48px; }
                section { background: #fff; border-radius: 10px; padding: 20px 24px; margin-bottom: 20px;
                          box-shadow: 0 1px 3px rgba(0,0,0,.08); }
                h2 { margin-top: 0; font-size: 1.25rem; border-bottom: 2px solid #e4e7eb; padding-bottom: 8px; }
                .status { display: inline-block; background: #d1fae5; color: #065f46; padding: 4px 12px;
                          border-radius: 999px; font-weight: 600; }
                .table-wrap { overflow-x: auto; }
                table { width: 100%; border-collapse: collapse; min-width: 520px; }
                th, td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #e4e7eb; vertical-align: top; }
                th { background: #f0f2f5; }
                code { background: #eef1f5; padding: 2px 6px; border-radius: 4px; font-size: .9em; }
                .m { font-weight: 700; font-size: .8rem; padding: 2px 8px; border-radius: 4px; color: #fff; }
                .GET { background: #2563eb; } .POST { background: #16a34a; }
                .PUT { background: #d97706; } .DELETE { background: #dc2626; }
                @media (max-width: 600px) {
                  header { padding: 28px 16px; } header h1 { font-size: 1.5rem; }
                  section { padding: 16px; }
                }
              </style>
            </head>
            <body>
              <header>
                <h1>Welcome to CineMatch</h1>
                <p>CineMatch is a web app for movie lovers who want one place to discover, rate and keep track
                   of films. Users search a large movie catalog and rate movies from 1 to 5, and the system
                   helps them decide what to watch next.</p>
              </header>
              <main>
                <section>
                  <h2>Status</h2>
                  <span class="status">API is running</span> &nbsp; Version: <code>v1</code>
                </section>

                <section>
                  <h2>Team (Group 12)</h2>
                  <div class="table-wrap">
                  <table>
                    <tr><th>Name</th><th>Student ID</th><th>What they built</th></tr>
                    <tr><td>Konul Abbasova</td><td>TODO</td><td>TODO</td></tr>
                    <tr><td>Aytaj Mammadova</td><td>TODO</td><td>TODO</td></tr>
                    <tr><td>Khadija Mirzayeva</td><td>TODO</td><td>TODO</td></tr>
                  </table>
                  </div>
                </section>

                <section>
                  <h2>Available endpoints</h2>
                  <div class="table-wrap">
                  <table>
                    <tr><th>Method</th><th>Path</th><th>Description</th></tr>
                    <tr><td><span class="m GET">GET</span></td><td><code>/api/v1/greeting</code></td><td>This HTML page (public).</td></tr>
                    <tr><td><span class="m GET">GET</span></td><td><code>/health</code></td><td>Health check, returns status UP (public).</td></tr>
                    <tr><td><span class="m GET">GET</span></td><td><code>/api/v1/movies</code></td><td>List movies. Filters: <code>search</code>, <code>genre</code>, <code>year</code>, <code>page</code>, <code>perPage</code> (public).</td></tr>
                    <tr><td><span class="m GET">GET</span></td><td><code>/api/v1/movies/{id}</code></td><td>Get one movie with full details (public).</td></tr>
                    <tr><td><span class="m GET">GET</span></td><td><code>/api/v1/ratings</code></td><td>List own ratings (admin: all). Token required.</td></tr>
                    <tr><td><span class="m GET">GET</span></td><td><code>/api/v1/ratings/{id}</code></td><td>Get one rating. Token required.</td></tr>
                    <tr><td><span class="m POST">POST</span></td><td><code>/api/v1/ratings</code></td><td>Create a rating for a movie. Token required.</td></tr>
                    <tr><td><span class="m PUT">PUT</span></td><td><code>/api/v1/ratings/{id}</code></td><td>Update a rating. Token required.</td></tr>
                    <tr><td><span class="m DELETE">DELETE</span></td><td><code>/api/v1/ratings/{id}</code></td><td>Delete a rating. Token required.</td></tr>
                  </table>
                  </div>
                </section>
              </main>
            </body>
            </html>
            """;
}
