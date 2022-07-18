if (process.env.NODE_ENV === "production") {
    const opt = require("./doodle-explore-2-opt.js");
    opt.main();
    module.exports = opt;
} else {
    var exports = window;
    exports.require = require("./doodle-explore-2-fastopt-entrypoint.js").require;
    window.global = window;

    const fastOpt = require("./doodle-explore-2-fastopt.js");
    fastOpt.main()
    module.exports = fastOpt;

    if (module.hot) {
        module.hot.accept();
    }
}
