{
  description = "PDQ";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    mvn2nix.url = "github:fzakaria/mvn2nix";
  };

  outputs = { self, nixpkgs, flake-utils, mvn2nix, ... }@inputs:
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          # import nix packages
          pkgs = import nixpkgs {
            inherit system;
          };
         
          # maven repository of dependencies
          # mavenRepository = mvn2nix.buildMavenRepositoryFromLockFile
          #   {
          #     file = ./mvn2nix-lock.json;
          #   };
          
          # Utility to run a script easily in the flakes app
          simple_script = name: add_deps: text: let
            exec = pkgs.writeShellApplication {
              inherit name text;
              runtimeInputs = with pkgs; [
                jdk11
                maven
              ] ++ add_deps;
            };
          in {
            type = "app";
            program = "${exec}/bin/${name}";
          };

        in with pkgs;
          {
            ###################################################################
            #                             scripts                             #
            ###################################################################
            apps = {
              default = simple_script "create_mvn_lockfile" [] ''
                     nix run github:fzakaria/mvn2nix#mvn2nix > mvn2nix-lock.json 
              '';

            };

            ###################################################################
            #                             build jar                           #
            ###################################################################
            packages = {
              default =  stdenv.mkDerivation rec {
                name = "PDQ";
                nativebBuildInputs = [ makeWrapper ];
                buildInputs = [ maven ];                
                src = ./.;
                buildPhase = ''
                  mvn clean package -DskipTests -Dmaven.repo.local=$out
               '';
                installPhase = ''
    mkdir -p $out/bin $out/share/pdq
    install -Dm644 pdq-main/target/pdq-main-2.0.0-jar-with-dependencies.jar $out/share/pdq/pdq.jar

    echo "${jre}/bin/java -jar $out/share/pdq/pdq.jar" > $out/bin/pdq
    chmod 744 $out/bin/pdq
  '';                  
              };

              
              # default = maven.buildMavenPackage rec {
              #   pname = "pdq";
              #   version = "2.0.0";

                
                
              #   buildPhase = ''
              #       echo "MMMMMMMMMMMMMMMMMMMYYYYY MAVEN"

              #   '';
                
              #   src = ./.;
              #   # src = fetchFromGitHub {
              #   #   owner = "intoolswetrust";
              #   #   repo = pname;
              #   #   rev = "${pname}-${version}";
              #   #   hash = "sha256-rRttA5H0A0c44loBzbKH7Waoted3IsOgxGCD2VM0U/Q=";
              #   # };

              #   mvnHash = "sha256-kLpjMj05uC94/5vGMwMlFzLKNFOKeyNvq/vmB6pHTAo=";

              #   nativeBuildInputs = [ makeWrapper ];

              #   meta = with lib; {
              #     description = "PDQ";
              #     homepage = "https://github.com/lordpretzel/PDQ";
              #     license = licenses.gpl3Plus;
              #     #maintainers = with maintainers; [ majiir ];
              #   };
              # };
            };

            
            ###################################################################
            #                       development shell                         #
            ###################################################################
            devShells.default =
              mkShell
                {
                  nativeBuildInputs = with pkgs; [
                    jdk11
                    maven
                    python311
                  ];
                };
          }
      );
}
