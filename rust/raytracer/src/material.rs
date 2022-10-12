use crate::{Color, get_random_double, Ray};
use crate::hittable::HitRecord;
use crate::utils::sq;
use crate::vec3::{dot, random_in_unit_sphere, random_unit_vector, reflect, refract};

pub trait Material {
    fn scatter(&self, ray_in: &Ray, hit_record: &HitRecord) -> Option<(Color, Ray)>;
}

#[derive(Debug, Copy, Clone)]
pub struct Lambertian {
    pub albedo: Color,
}

impl Lambertian {
    pub fn new(albedo: Color) -> Self {
        Self { albedo }
    }
}

impl Material for Lambertian {
    fn scatter(&self, _ray_in: &Ray, hit_record: &HitRecord) -> Option<(Color, Ray)> {
        let mut scatter_direction = hit_record.normal + random_unit_vector();

        if scatter_direction.near_zero() {
            scatter_direction = hit_record.normal;
        }

        let scattered = Ray::new(hit_record.point, scatter_direction);
        Some((self.albedo, scattered))
    }
}

#[derive(Debug, Copy, Clone)]
pub struct Metal {
    pub albedo: Color,
    pub fuzz: f64,
}

impl Metal {
    pub fn new(albedo: Color, fuzz: f64) -> Self {
        Self { albedo, fuzz: if fuzz < 1.0 { fuzz } else { 1.0 } }
    }
}

impl Material for Metal {
    fn scatter(&self, ray_in: &Ray, hit_record: &HitRecord) -> Option<(Color, Ray)> {
        let reflected = reflect(ray_in.dir.unit_vector(), hit_record.normal);
        let scattered = Ray::new(hit_record.point, reflected + self.fuzz * random_in_unit_sphere());
        if dot(&scattered.dir, &hit_record.normal) > 0.0 {
            Some((self.albedo, scattered))
        } else {
            None
        }
    }
}

#[derive(Debug, Copy, Clone)]
pub struct Dielectric {
    pub refraction_index: f64,
}

impl Dielectric {
    pub fn new(refraction_index: f64) -> Self {
        Self { refraction_index }
    }

    fn reflectance(cosine: f64, refraction_index: f64) -> f64 {
        let r0 = sq((1.0 - refraction_index) / (1.0 + refraction_index));
        r0 + (1.0 - r0) * (1.0 - cosine).powi(5)
    }
}

impl Material for Dielectric {
    fn scatter(&self, ray: &Ray, hit_record: &HitRecord) -> Option<(Color, Ray)> {
        let attenuation = Color::new(1.0, 1.0, 1.0);
        let refraction_ratio =
            if hit_record.front_face { 1.0 / self.refraction_index } else { self.refraction_index };

        let ray_unit_direction = ray.dir.unit_vector();
        let cos_theta = dot(&-ray_unit_direction, &hit_record.normal).min(1.0);
        let sin_theta = (1.0 - sq(cos_theta)).sqrt();

        let direction = if refraction_ratio * sin_theta > 1.0
            || Dielectric::reflectance(cos_theta, refraction_ratio) > get_random_double() {
            reflect(ray_unit_direction, hit_record.normal)
        } else {
            refract(ray_unit_direction, hit_record.normal, refraction_ratio)
        };

        let scattered = Ray::new(hit_record.point, direction);
        Some((attenuation, scattered))
    }
}