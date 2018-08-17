package pro.javatar.pipeline.builder

class YamlModel {

    Npm npm = new Npm()

    Npm getNpm() {
        return npm
    }

    void setNpm(Npm npm) {
        this.npm = npm
    }

    @Override
    public String toString() {
        return "YamlModel{" +
                "npm=" + npm +
                '}';
    }
}