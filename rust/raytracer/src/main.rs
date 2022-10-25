use std::f64::consts::PI;
use image::{ImageBuffer, Rgb};

use crate::camera::Camera;
use crate::equation::QuadraticEquation;
use crate::hittable::{Hittable, HittableList};
use crate::material::{Dielectric, Lambertian, Light, Metal};
use crate::ray::Ray;
use crate::sphere::Sphere;
use crate::texture::SolidColor;
use crate::vec3::{BLACK, Color, Vec3};
use crate::utils::get_random_double;
use crate::plane::Plane;

pub mod equation;
pub mod sphere;
pub mod hittable;
pub mod ray;
pub mod vec3;
pub mod material;
pub mod camera;
pub mod utils;
pub mod texture;
pub mod plane;
pub mod aabb;

#[allow(unused_variables, dead_code)]
fn main() {
    let ratio = 16.0 / 9.0;
    let width = 1000;
    let height = (width as f64 / ratio) as u32;
    let samples_per_pixel = 10000;
    let max_depth = 50;
    let color = BLACK;

    let r = (PI / 4.0).cos();

    // let (scene, camera) = scene1(ratio);
    let (scene, camera) = scene2(ratio);

    let mut img = image::ImageBuffer::new(width, height);

    render(&mut img, &scene, &camera, color, &samples_per_pixel, &max_depth);
    img.save("images/img.png").unwrap();
}

fn scene2(ratio: f64) -> (HittableList, Camera) {
    let camera = Camera::new(
        Vec3::new(26.0, 3.0, 6.0),
        Vec3::new(0.0, 2.0, 0.0),
        Vec3::new(0.0, 1.0, 0.0),
        20.0, ratio);
    let mut scene: HittableList = HittableList::new();

    // TODO: cannot pass material with reference
    let material: Lambertian<SolidColor> = Lambertian::new(SolidColor::new(0.8, 0.8, 0.0));
    scene.add(Sphere::new(Vec3::new(0.0, 2.0, 0.0), 2.0, material));

    let light: Light<SolidColor> = Light::new(SolidColor::new(4.0, 4.0, 4.0));
    scene.add(Plane::new(3.0, 5.0, 1.0, 3.0, -2.0, light));

    (scene, camera)
}

fn scene1(ratio: f64) -> (HittableList, Camera) {
    let camera = Camera::new(
        Vec3::new(-2.0, 2.0, 1.0),
        Vec3::new(0.0, 0.0, -1.0),
        Vec3::new(0.0, 1.0, 0.0),
        50.0, ratio);
    let mut scene: HittableList = HittableList::new();

    let material_ground: Lambertian<SolidColor> = Lambertian::new(SolidColor::new(0.8, 0.8, 0.0));
    let material_center: Lambertian<SolidColor> = Lambertian::new(SolidColor::new(0.1, 0.2, 0.5));
    let material_left: Dielectric = Dielectric::new(1.5);
    let material_right: Metal<SolidColor> = Metal::new(SolidColor::new(0.8, 0.6, 0.2), 0.0);

    scene.add(Sphere::new(Vec3::new(0.0, -100.5, -1.0), 100.0, material_ground));
    scene.add(Sphere::new(Vec3::new(0.0, 0.0, -1.0), 0.5, material_center));
    scene.add(Sphere::new(Vec3::new(-1.0, 0.0, -1.0), 0.5, material_left));
    scene.add(Sphere::new(Vec3::new(1.0, 0.0, -1.0), 0.5, material_right));

    let light: Light<SolidColor> = Light::new(SolidColor::new(4.0, 4.0, 4.0));
    scene.add(Sphere::new(Vec3::new(0.0, 3.0, 0.0), 1.0, light));

    (scene, camera)
}

pub fn render(image: &mut ImageBuffer<Rgb<u8>, Vec<u8>>,
              scene: &HittableList,
              camera: &Camera,
              color: Color,
              samples_per_pixel: &u32,
              max_depth: &u32,
) {
    let (width, height) = image.dimensions();
    for (i, j, pixel) in image.enumerate_pixels_mut() {
        let mut color_pixel = BLACK;
        for _ in 0..*samples_per_pixel {
            let u = (i as f64 + get_random_double()) / (width - 1) as f64;
            let v = (j as f64 + get_random_double()) / (height - 1) as f64;
            let ray = camera.get_ray(u, v);
            color_pixel += cast_ray(&ray, color, scene, max_depth);
        }
        *pixel = get_color(&color_pixel, samples_per_pixel);
    }
}

fn get_color(color: &Color, samples_per_pixel: &u32) -> Rgb<u8> {
    let scale = 1.0 / *samples_per_pixel as f64;
    let r = (color.x * scale).sqrt();
    let g = (color.y * scale).sqrt();
    let b = (color.z * scale).sqrt();
    Rgb([(256.0 * clamp(r, 0.0, 0.999)) as u8,
        (256.0 * clamp(g, 0.0, 0.999)) as u8,
        (256.0 * clamp(b, 0.0, 0.999)) as u8])
}

fn clamp(x: f64, min: f64, max: f64) -> f64 {
    if x < min { return min; }
    if x > max { return max; }
    x
}

pub fn cast_ray(ray: &Ray, color: Color, scene: &HittableList, depth: &u32) -> Color {
    if *depth == 0 {
        return BLACK;
    }
    match scene.hit(ray, 0.001, f64::MAX) {
        Some(rec) => {
            let emitted = rec.material.emitted(rec.u, rec.v, rec.point);
            return match rec.material.scatter(ray, &rec) {
                Some((attenuation, scattered)) =>
                    emitted + attenuation * cast_ray(&scattered, color, scene, &(depth - 1)),
                None => emitted
            };
        }
        None => {
            // TODO: remove comments for background color
            color
            // let unit_dir = ray.dir.unit_vector();
            // let t = 0.5 * (unit_dir.y + 1.0);
            // (1.0 - t) * Color::new(1.0, 1.0, 1.0) + t * Color::new(0.5, 0.7, 1.0)
        }
    }
}
